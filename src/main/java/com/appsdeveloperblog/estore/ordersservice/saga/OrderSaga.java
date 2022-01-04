package com.appsdeveloperblog.estore.ordersservice.saga;

import com.appsdeveloperblog.estore.ordersservice.command.models.ApproveOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.models.RejectOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderRejectedEvent;
import com.appsdeveloperblog.estore.sagacoreapi.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.estore.sagacoreapi.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.estore.sagacoreapi.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.sagacoreapi.events.PaymentProcessedEvent;
import com.appsdeveloperblog.estore.sagacoreapi.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.estore.sagacoreapi.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.sagacoreapi.models.User;
import com.appsdeveloperblog.estore.sagacoreapi.query.FetchUserPaymentDetailsQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Saga
public class OrderSaga {
    private final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE = "payment-processing-deadline";
    // Since @Saga creates serialization use transient
    // to prevent serialization of fields.
    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;
    @Autowired
    private transient DeadlineManager deadlineManager;
    private String scheduleId;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        log.info("OrderCreatedEvent handled for orderId: " + reserveProductCommand.getOrderId() +
                " and productId: " + reserveProductCommand.getProductId());

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {

            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
                                 CommandResultMessage<? extends Object> commandResultMessage) {
                if (commandResultMessage.isExceptional()) {
                    // Start a compensating transaction
                    log.error("Starting a compensating transaction. "
                            + commandResultMessage.exceptionResult().getMessage());

                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderCreatedEvent.getOrderId(),
                            commandResultMessage.exceptionResult().getMessage());

                    commandGateway.send(rejectOrderCommand);


                }
            }
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {
        // Process user payment
        log.info("ProductReservedEvent handled for productId: " + productReservedEvent.getProductId() +
                " and orderId: " + productReservedEvent.getOrderId());

        FetchUserPaymentDetailsQuery FetchUserPaymentDetailsQuery =
                new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());

        User userPaymentDetails = null;

        try {
            userPaymentDetails = queryGateway.query(FetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception ex) {
            log.error("User Payment details: " + ex.getMessage());

            // Start compensating transaction
            cancelProductReservation(productReservedEvent, ex.getMessage());
            return;
        }

        if (userPaymentDetails == null) {
            log.error("User Payment details are null ");
            // Start compensating transaction
            cancelProductReservation(productReservedEvent, "User Payment details are null ");
            return;
        }

        log.info("Successfully fetched user payment details for user " + userPaymentDetails.getFirstName());

        // execute a task after a set amount of time, similar to a Cron job.
        // sent userPaymentDetails
        scheduleId = deadlineManager.schedule(Duration.of(10, ChronoUnit.SECONDS),
                PAYMENT_PROCESSING_TIMEOUT_DEADLINE, userPaymentDetails);

        // TODO uncomment out to test deadline manager functionality
        // if (true) return;

        // Here is where you process the payment after Reserving product and fetching payment details from
        // user
        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(userPaymentDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            // Start compensating transaction
            log.error("Starting compensating transaction " + ex.getMessage());
            cancelProductReservation(productReservedEvent, ex.getMessage());
            return;
        }

        if (result == null) {
            // Start compensating transaction
            log.error("The ProcessPaymentCommand resulted in NULL. Initiating a compensating transaction ");
            cancelProductReservation(productReservedEvent, "The ProcessPaymentCommand resulted in NULL.");
        }

    }

    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {
        // If recieved paymentProcessedEvent successfully cancel Deadline Manager Payment timeout.
        cancelDeadline();

        CancelProductReservationCommand publishCancelProductReservationCommand =
                CancelProductReservationCommand.builder()
                        .orderId(productReservedEvent.getOrderId())
                        .productId(productReservedEvent.getProductId())
                        .quantity(productReservedEvent.getQuantity())
                        .userId(productReservedEvent.getUserId())
                        .reason(reason)
                        .build();

        commandGateway.send(publishCancelProductReservationCommand);
    }

    // For payment process event
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {
        // If recieved paymentProcessedEvent successfully cancel Deadline Manager Payment timeout.
        cancelDeadline();
        // Send an ApprovedOrderCommand
        ApproveOrderCommand approveOrderCommand =
                new ApproveOrderCommand(paymentProcessedEvent.getOrderId());

        commandGateway.send(approveOrderCommand);
    }

    private void cancelDeadline() {
        if (scheduleId != null) {
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_TIMEOUT_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        log.info("Order is approved. Order saga is complete for order id: " + orderApprovedEvent.getOrderId());
        // another way to end Saga life cycle instead of using annotation
        // can add custom logic to end the Saga life cycle based on certain conditions.
        // SagaLifecycle.end();
    }

    // new event handlers to start event that order has been cancelled
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {
        // Create and send a RejectOrderCommand
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCancelledEvent.getOrderId(),
                productReservationCancelledEvent.getReason());

        commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent) {
        log.info("Successfully rejected order with id " + orderRejectedEvent.getOrderId());
    }

    // Deadline handlers method
    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent) {
        log.info("Payment processing deadline took place. Sending a compensating " +
                "command to cancel the product reservation");

        cancelProductReservation(productReservedEvent, "Payment timeout");
    }
}
