package org.libraryexpress.infrastructure.cli;

import org.libraryexpress.application.customer.dto.request.UpdateCustomerEmailDto;
import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.application.customer.usecase.CreateCustomer;
import org.libraryexpress.application.customer.usecase.FindCustomer;
import org.libraryexpress.application.customer.usecase.ListCustomers;
import org.libraryexpress.application.customer.usecase.UpdateCustomerEmail;
import org.libraryexpress.application.customer.dto.request.CreateCustomerDto;
import org.libraryexpress.domain.customer.exception.CustomerNotFoundException;
import org.libraryexpress.domain.customer.exception.UniqueEmailViolationException;
import org.libraryexpress.infrastructure.config.AppContext;
import org.libraryexpress.infrastructure.config.logging.LogTrace;
import org.libraryexpress.infrastructure.util.JsonPrinter;

import java.util.Scanner;

class CustomerCli {

    private final CreateCustomer createCustomer;
    private final FindCustomer findCustomer;
    private final ListCustomers listCustomers;
    private final UpdateCustomerEmail updateEmail;

    public CustomerCli(AppContext context) {
        this.createCustomer = context.getCreateCustomer();
        this.findCustomer = context.getFindCustomer();
        this.listCustomers = context.getListCustomers();
        this.updateEmail = context.getUpdateCustomerEmail();
    }

    public void init(Scanner scan) {
        var loop = true;

        do {
            System.out.println(" ");
            System.out.println("[1] - New");
            System.out.println("[2] - Show");
            System.out.println("[3] - Update");
            System.out.println("[4] - List");
            System.out.println("[6] - Back");
            System.out.println(" ");

            int option = scan.nextInt();

            switch (option) {
                case 1 -> this.create(scan);
                case 2 -> this.show(scan);
                case 3 -> this.update(scan);
                case 4 -> this.list();
                case 6 -> loop = false;
                default -> System.out.println("Invalid option!");
            }

        } while (loop);
    }

    private void create(Scanner scan) {
        LogTrace.start();

        scan.nextLine();

        System.out.println("Enter the customer name:");
        String name = scan.nextLine().trim();
        System.out.println("  ");

        System.out.println("Enter the customer e-mail:");
        String email = scan.nextLine().trim();
        System.out.println("  ");

        CreateCustomerDto createCustomerDto = new CreateCustomerDto(name, email);

        try {
            this.createCustomer.execute(createCustomerDto);

            System.out.println(" ");
            System.out.println("Customer registered successfully!");

        } catch (UniqueEmailViolationException e) {

            System.out.println(e.getMessage());

        } catch (Exception e) {
            System.out.println(" ");
            System.out.println("Unexpected error has occurred:");
            System.out.println(e.getMessage());
        } finally {
            LogTrace.clear();
        }
    }

    private void show(Scanner scan) {
        LogTrace.start();

        System.out.println("Enter the customer e-mail or ID");
        String dataToSearch = scan.next();

        try {
            CustomerDto customerDto = this.findCustomer.execute(dataToSearch);
            System.out.println(JsonPrinter.print(customerDto));

        } catch (CustomerNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            LogTrace.clear();
        }

    }

    private void update(Scanner scan) {
        LogTrace.start();

        System.out.println("Enter the customer ID");
        String id = scan.next();

        try {
            System.out.println("Enter the new customer e-mail");
            String newEmail = scan.next();

            UpdateCustomerEmailDto updateEmail = new UpdateCustomerEmailDto(id, newEmail);

            this.updateEmail.execute(updateEmail);

            System.out.println("Updated successfully!");

        } catch (CustomerNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(" ");
            System.out.println("Unexpected error has occurred:");
            System.out.println(e.getMessage());
        } finally {
            LogTrace.clear();
        }
    }

    private void list() {
        LogTrace.start();
        var customers = this.listCustomers.execute(null);

        if (customers.items().isEmpty()) {
            System.out.println("No clients found.");
        } else {
            System.out.println(JsonPrinter.print(customers));
        }

        LogTrace.clear();
    }
}
