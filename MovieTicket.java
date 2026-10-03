package javaproject;


	import java.util.Scanner;

	class Booking {

	    String bookingId;
	    String movieName;
	    String seats;
	    double amount;

	    Booking(String bookingId, String movieName, String seats, double amount) {
	        this.bookingId = bookingId;
	        this.movieName = movieName;
	        this.seats = seats;
	        this.amount = amount;
	    }

	    void showBooking() {
	        System.out.println("\n BOOKING DETAILS ");
	        System.out.println("Booking ID : " + bookingId);
	        System.out.println("Movie      : " + movieName);
	        System.out.println("Seats      : " + seats);
	        System.out.println("Total      : Rs." + amount);
	    }
	}


	class Theatre {

	    private boolean[][] seats = new boolean[5][6];

	    private int bookingNumber = 1001;

	    private double totalCollection = 0;

	    private String movieName = "Avengers";


	    public void displaySeats() {

	        System.out.println("\n AVAILABLE SEATS ");

	        for (int i = 0; i < 5; i++) {

	            char row = (char) ('A' + i);

	            for (int j = 0; j < 6; j++) {

	                String seat = row + String.valueOf(j + 1);

	                if (seats[i][j]) {
	                    System.out.print("[XX] ");
	                } else {
	                    System.out.print("[" + seat + "] ");
	                }
	            }

	            System.out.println();
	        }
	    }


	    private int getRow(String seat) {

	        return Character.toUpperCase(seat.charAt(0)) - 'A';
	    }


	    private int getColumn(String seat) {

	        return Integer.parseInt(seat.substring(1)) - 1;
	    }


	    private boolean validSeat(String seat) {

	        try {

	            int row = getRow(seat);
	            int column = getColumn(seat);

	            return row >= 0 && row < 5 &&
	                   column >= 0 && column < 6;

	        } catch (Exception e) {

	            return false;
	        }
	    }


	    public void bookMultipleSeats(String input, boolean weekend) {

	        String[] selectedSeats = input.toUpperCase().split("\\s+");


	        

	        for (String seat : selectedSeats) {

	            if (!validSeat(seat)) {

	                System.out.println("Invalid seat: " + seat);
	                return;
	            }

	            int row = getRow(seat);
	            int column = getColumn(seat);

	            if (seats[row][column]) {

	                System.out.println("Seat " + seat + " is already booked.");
	                return;
	            }
	        }


	        double total = 0;

	        String bookedSeats = "";


	       

	        for (String seat : selectedSeats) {

	            int row = getRow(seat);

	            double price;

	            if (row >= 3) {
	                price = 250;
	            } else {
	                price = 180;
	            }

	            if (weekend) {
	                price = price + 50;
	            }

	            total = total + price;

	            bookedSeats = bookedSeats + seat + " ";
	        }


	        

	        for (String seat : selectedSeats) {

	            int row = getRow(seat);
	            int column = getColumn(seat);

	            seats[row][column] = true;
	        }


	        String bookingId = "BK" + bookingNumber;

	        bookingNumber++;

	        totalCollection = totalCollection + total;


	        Booking booking = new Booking(
	                bookingId,
	                movieName,
	                bookedSeats,
	                total
	        );

	        booking.showBooking();
	    }


	    public void cancelSeats(String input) {

	        String[] selectedSeats = input.toUpperCase().split("\\s+");

	        for (String seat : selectedSeats) {

	            if (!validSeat(seat)) {

	                System.out.println("Invalid seat: " + seat);
	                continue;
	            }

	            int row = getRow(seat);
	            int column = getColumn(seat);

	            if (!seats[row][column]) {

	                System.out.println(
	                        "Seat " + seat + " is not booked."
	                );

	            } else {

	                seats[row][column] = false;

	                System.out.println(
	                        "Seat " + seat + " cancelled successfully."
	                );
	            }
	        }
	    }


	    public void showCollection() {

	        System.out.println("\n COLLECTION ");

	        System.out.println("Movie      : " + movieName);

	        System.out.println(
	                "Collection : Rs." + totalCollection
	        );
	    }
	}


	public class MovieTicket {

	    public static void main(String[] args) {

	        Scanner sc = new Scanner(System.in);

	        Theatre theatre = new Theatre();


	        while (true) {

	            
	            System.out.println("     MOVIE TICKET SYSTEM    ");

	            System.out.println("1. Show Seats");
	            System.out.println("2. Book Multiple Seats");
	            System.out.println("3. Cancel Seats");
	            System.out.println("4. Show Collection");
	            System.out.println("5. Exit");

	            System.out.print("Enter choice: ");

	            int choice = sc.nextInt();


	            switch (choice) {


	                case 1:

	                    theatre.displaySeats();

	                    break;


	                case 2:

	                    sc.nextLine();

	                    System.out.print(
	                            "Enter seats (example: A2 A3 A4): "
	                    );

	                    String seatInput = sc.nextLine();


	                    System.out.print(
	                            "Is it weekend? (yes/no): "
	                    );

	                    String answer = sc.next();

	                    boolean weekend =
	                            answer.equalsIgnoreCase("yes");


	                    theatre.bookMultipleSeats(
	                            seatInput,
	                            weekend
	                    );

	                    break;


	                case 3:

	                    sc.nextLine();

	                    System.out.print(
	                            "Enter seats to cancel: "
	                    );

	                    String cancelInput = sc.nextLine();

	                    theatre.cancelSeats(cancelInput);

	                    break;


	                case 4:

	                    theatre.showCollection();

	                    break;


	                case 5:

	                    System.out.println(
	                            "Thank you for using the Movie Ticket System!"
	                    );

	                    sc.close();

	                    return;


	                default:

	                    System.out.println(
	                            "Invalid choice."
	                    );
	            }
	        }
	    }
	}