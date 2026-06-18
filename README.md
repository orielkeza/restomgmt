Restaurant management system

task to do
the task is to create a system which will do the management of Restaurant from receptionist to finance manager to CEO of the restaurant.

user requirement with each entity role and activity

We will have a web app for the client
1.client can order food or drink directly from the restaurant and also outside of restaurant
2.client can register themselves into the system with the following details
	1.username,email,phonenumber
	2.Password which has the following constraints (include upper case letter, username not included in password,>=1 special character,>=one number)
	3.username must be unique
3. client will be able to login using those username
	1.on the first login in 3 Month must sent OTP to the client
	2.make sure auditing is enabled with status of failed or success and browser and also client IP address.
	3.when user failed to login 3 times open up OTP auth for checking if he is the one trying to login and force user to change password
4. client can change the password
5. client can order foods online
	1.Client will login into the system and view our menus of food or drinks and choose which foods he want to order
		by entering quantity and then place order
	2.Client can add more items to cart before processed to purchase which means will be able to select one item and it to card and choose other items and add it to cart and so on ....
	 	while user is viewing details of the product suggest similar product to help user to buy more in easy way try to find a way in which  you will do your presentation
	3.cart can keeps those information for as long as 3 Month after that from those item into cart and cart for the client will be empty.
	
	4. suppose we have tables in our VIP restaurant with table number so our client will be able to book a seat with at least one meal
		which means client will have option to book a seat by choosing  seat number from list and then choose a meal and the time from to be sure of when the client want to use that space
		seat number will be booked only once the client has paid otherwise will be available for others to book it and be sure you displayed that message on the UI whenever someone else booked it please ask the user to change the seat number bcs that one is booked.
	5. do the integration with MTN mobile money as MTN mobile money has it's api available for testing purpose use it and integrate it into our system.
	6. client will receive different notification
		-notification related to change of password.
		-notification related to payment status either successful or failed.
		-Sent a client notification before 15 min according to booked seat.
	7. Client will be able to view his/cart, purchased product.
	8. client will be able to track the order and view it's status
	9.client will be able to cancel order before 30min and we will revert back the money to the user.
we will have a web portal for staff
	1. create a default admin account to access the system panel
	2. admin will be able to create other users and add them roles, permission of particular menus, actions
		which mean roles will be assigned to user and
		role will be assigned user actions, menus
		which means whoever who have a particular roles will be able to view menus assigned to him and actions assigned to him
	put that action under security menu.
	3. user will be able to view orders submitted, with it's details
	4. user will be able to view seat booked with it's time and details of(meals, drinks)
	5. user will be able to register our seat (seat number, and number of chair it has)
		user will be able to mark a seat as available again in case it was busy before so that other user can use it.
	6. user will have the page to view payments and it's details
	7. user will be able to view registered clients with actions of disabling user account.
	8. user will be able to register meals or foods in our system so that our clients can order food plz take a reference from other food related applications.
	9. Give a user a different report related to what we have in the system (think about it yourself and let's see which report is appropriate)
	10. user will have different notifcation
		-before 1hour and also before 15min again send notificatin reminding the user that there is a booking and of the meals.
		-send 30min notificaiton to the user about the order.
	11. user will confirm in the system saying that the order is on the way
	with that status and user will register the phone number of the rider.
	
Technical specifications

1.provide a database diagram

2. provide figma prototype for the most important flow of our application.

3. create pipeline on the github which will run your application test case atleast on the coverage of 90% and also
check code quality like possible nullpointer exception to be thrown,unnecessary if conditions and so on ....

4. create backend by using spring boot

use oauth2 for authentication remember to add capability that user can register using google account
user postgresql as your database
user mongodb for auditing
provides an asynchronous, event-driven way to deliver messages (`SMS, push alerts) without slowing down our main application(use RabbitMq)
create testing every method in your project should have a negative and positive test case
5. in our application use docker container so that even me who don't have you application can run it without facing any issue
6. in our application we are going to use reactjs as our frontend framework with  typescript for the code quality and also for the state management use Redux
remember to protect the route in our system.
remember to use internationalization for messages
-from frontend pass request using lang
-even errors should have diff languages
-start with English and french

profile management
# Run tests with test profile
mvn test -Dspring.profiles.active=test

# Run app in dev
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run app in prod
java -Dspring.profiles.active=prod -jar target/site-0.0.1-SNAPSHOT.jar
