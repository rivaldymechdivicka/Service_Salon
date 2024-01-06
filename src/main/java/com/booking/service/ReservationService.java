package com.booking.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import com.booking.models.Customer;
import com.booking.models.Employee;
import com.booking.models.Person;
import com.booking.models.Reservation;
import com.booking.models.Service;
import com.booking.repositories.ServiceRepository;

public class ReservationService {

    private static List<Service> serviceList = ServiceRepository.getAllService();
    private static Scanner input = new Scanner(System.in);

    private static PrintService printService = new PrintService();

    public static void createReservation(List<Reservation> reservationList, List<Person> personList) {
        printService.showAllCustomer(personList);
        System.out.println("Enter customer ID for reservation: ");
        String customerId = input.nextLine();

        Customer customer = findCustomerById(customerId, personList);

        while (customer == null) {
            System.out.println("Customer is not available!");
            System.out.println("Enter customer ID that exist for reservation: ");
            customerId = input.nextLine();
            customer = findCustomerById(customerId, personList);
        }

        printService.showAllEmployee(personList);
        System.out.println("Enter employee ID for reservation: ");
        String employeeId = input.nextLine();

        Employee employee = findEmployeeById(employeeId, personList);

        while (employee == null) {
            System.out.println("Employee is not available!");
            System.out.println("Enter employee ID that exist for reservation: ");
            employeeId = input.nextLine();
            employee = findEmployeeById(employeeId, personList);
        }

        List<Service> selectedServices = selectServices();

        String workstage = "In Process";

        String reservationId = "Res-" + UUID.randomUUID().toString().substring(0, 6);
        double reservationPrice = calculateReservationPrice(selectedServices, customer);

        Reservation reservation = new Reservation(reservationId, customer, employee, selectedServices,
                workstage);
        reservation.setReservationPrice(reservationPrice);

        reservationList.add(reservation);
        System.out.println("Reservation created successfully!");
    }

    public static void editReservationWorkstage(List<Reservation> reservationList, List<Person> personList) {
        printService.showRecentReservation(reservationList);
        System.out.println("Enter reservation ID to edit workstage: ");
        String reservationId = input.nextLine();

        Reservation reservation = findReservationById(reservationId, reservationList);

        while (reservation == null) {
            System.out.println("Reservation is not available!");
            System.out.println("Enter reservation ID that exist to edit workstage: (Input 0 to go back MainMenu)");
            reservationId = input.nextLine();

            if (reservationId.equals("0"))
                break;

            reservation = findReservationById(reservationId, reservationList);

            if (reservation.getWorkstage().equals("In Process"))
                break;

            System.out.println("This reservation is completed!");
        }

        if (reservation != null && !reservationId.equals("0")) {
            String newWorkstage = selectedWorkStage();

            double oldWallet = reservation.getCustomer().getWallet();

            if (newWorkstage.equals("Finish"))
                calculateCustomerWallet(reservation, personList);

            if (reservation.getCustomer().getWallet() != oldWallet){
                reservation.setWorkstage(newWorkstage);
                System.out.println("Workstage updated successfully!");
            } else {
                System.out.println("Workstage updated failed!");
            }

        } else if (!reservationId.equals("0")) {
            System.out.println("Reservation is not available!");
        }
    }

    private static Customer findCustomerById(String customerId, List<Person> personList) {
        return personList.stream()
                .filter(person -> person instanceof Customer && person.getId().equals(customerId))
                .map(person -> (Customer) person)
                .findFirst()
                .orElse(null);
    }

    private static Employee findEmployeeById(String employeeId, List<Person> personList) {
        return personList.stream()
                .filter(person -> person instanceof Employee && person.getId().equals(employeeId))
                .map(person -> (Employee) person)
                .findFirst()
                .orElse(null);
    }

    private static List<Service> selectServices() {
        printService.showAvailableService(serviceList);
        List<Service> selectedServices = new ArrayList<>();
        boolean loop = true;

        do {
            System.out.println("Enter service IDs for reservation: (Input 0 to stop choosing service)");
            String serviceIdsInput = input.nextLine();

            if (serviceIdsInput.equals("0"))
                break;

            Service service = findServiceById(serviceIdsInput, serviceList);

            if (service == null) {
                System.out.println("Service is not available!");
                continue;
            }

            if (!selectedServices.contains(service))
                selectedServices.add(service);
            else
                System.out.println("Service already selected!");

        } while (loop);

        return selectedServices;
    }

    private static double calculateReservationPrice(List<Service> selectedServices, Customer customer) {
        double totalPrice = selectedServices.stream()
                .mapToDouble(Service::getPrice)
                .sum();

        if (customer != null && customer.getMember() != null) {
            switch (customer.getMember().getMembershipName().toLowerCase()) {
                case "silver":
                    totalPrice *= 0.95; // Diskon 5%
                    break;
                case "gold":
                    totalPrice *= 0.90; // Diskon 10%
                    break;
            }
        }

        return totalPrice;
    }

    private static void calculateCustomerWallet(Reservation reservation, List<Person> personList) {
        Customer reservationCustomer = reservation.getCustomer();
        double servicePrice = reservation.getReservationPrice();
        Customer customer = new Customer();
        for (Person person : personList) {
            if (person instanceof Customer) {
                customer = (Customer) person;
                if (customer.getId().equals(reservationCustomer.getId())) {
                    double wallet = customer.getWallet() - servicePrice;
                    if (wallet < 0) {
                        System.out.println("Wallet is not enough");
                        break;
                    }
                    customer.setWallet(wallet);
                    break;
                }
            }
        }
    }

    private static Reservation findReservationById(String reservationId, List<Reservation> reservationList) {
        return reservationList.stream()
                .filter(reservation -> reservation.getReservationId().equals(reservationId))
                .findFirst()
                .orElse(null);
    }

    private static Service findServiceById(String serviceId, List<Service> serviceList) {
        return serviceList.stream()
                .filter(service -> service.getServiceId().equals(serviceId))
                .findFirst()
                .orElse(null);
    }

    public static String selectedWorkStage() {
        int choice = 0;
        String workstage = "";
        System.out.printf("Enter new workstage:%n1. %s%n2. %s%n3. %s%nYour choice: ", "In Process", "Finish",
                "Canceled");
        choice = Integer.valueOf(input.nextLine());
        while (choice > 3 || choice < 1) {
            System.out.println("WorkStage is not valid! Please Try Again:");
            choice = Integer.valueOf(input.nextLine());
        }
        switch (choice) {
            case 1:
                workstage = "In Process";
                break;
            case 2:
                workstage = "Finish";
                break;
            case 3:
                workstage = "Cancel";
                break;
        }
        return workstage;
    }

    // Silahkan tambahkan function lain, dan ubah function diatas sesuai kebutuhan
}
