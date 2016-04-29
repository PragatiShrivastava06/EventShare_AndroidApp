# EventShare_AndroidApp
Project Goal
Android application to manage daily events and task by sending event reminders to the friends, colleagues, clients and yourself (without the participants requiring to install the eventshare app)

Motivation & Use Cases (“What problem does it solve?”)  
Scenario 1:Manager effectively manages team meeting on the fly, setting scheduled reminders to alert participants of the meeting through SMS and/or email notifications.

Scenario 2: Invite friends and relatives for a party in two weeks by creating event at particular venue, time (sends event notification)  and schedule reminders (sends automatic reminders to all participants a day before the event).

Functionality and User Experience (“How does it help user to solve the problem?”) 
1.	Registration
2.	Calendar view - Displays existing events
3.	Create/Edit Event
a.	Title
b.	Event description-agenda, itinerary
c.	Venue
d.	Selecting participants from Contacts
e.	Time
f.	Profile name-Sender information
4.	Sending Invite - Email, sending SMS
5.	Sending reminder for meeting (Email, SMS)
6.	Additional Functionality (optional)-Event Management (enforces app installation by the participants)
a.	Accept/Reject meeting request-with description-via the app
b.	Event-Based Chat functionality
c.	  File sharing, minutes of the meeting, exchange party pictures

User Experience 
It provides a smooth interface for the user to plan and schedule day-to-day events on the fly and also keep the participants of the events on track with their oncoming schedule

Architecture (“How could it be implemented?”) 
MVC architecture
•	Technology used: Android Studio for creating the user interface. 
•	Calendar API- for creating, editing, updating, deleting events
•	Backend handled by Calendar API (built in Android SQLite)

Major Use Cases Implemented
1.	CreateEventActivity.java: This class provides the functionality to Create Event with events details like Title, Event description-agenda, venue, add attendees (based on mode i.e. SMS or email), time before which a reminder has to be sent, starting date and time and ending date and time.
a.	An email is also sent to the attendees on creation of the event.
b.	A reminder (email) will be sent to the organizer at the time set during the creation of the event
2.	On clicking done, an event is added to the default calendar and to the upcoming list of events. The java classes for the list of events is defined in the following classes:
•	CustomAdapter.java
•	EventActivity.java
3.	Clicking on any event on the list will open the event view, which will show the details of the selected event. The java class for viewing the event is ViewEventActivity.java
4.	EditActivity.java: This class provides the functionality to update/delete event with events details like Event description, location, attendees, time before which a reminder has to be sent, start date and end date, attachments.
5.	DeleteActivity.java: This class will delete an event.
6.	Reminder (Email/SMS): Email/SMS is sent as reminder to the attendees
7.	Attachment: Files/images can be added to the event.
8.	Group Chat: Event attendees can participate in a group chat.
Configuration for Google Places:
1.	Enable Google Places in your Google Developer Console for you project
2.	Generate the API key for the project package using the SHA1 Key
3.	Add the API key in your manifest like the following
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="A43k34FEFfaDFADFSFSD34343DFSFS" />
Demo Instruction
•	Copy the eventshare folder into the project folder
•	Open the project in Android Studio and run in on an android device
•	The android device should have a ‘Google Calendar’ and ‘Google account’ associated with it so that the app directly inserts event into the Google calendar as we are using Google API to achieve this.
•	In order to run the app, please follow the steps in the Youtube link.
•	Part 1:https://www.youtube.com/watch?v=g8I_k7oqQ7M&feature=youtu.be
•	Part2: https://www.youtube.com/watch?v=QVt-0BXovBU&feature=youtu.be

Points to Note:
The android device in which the app is running must have a Google account associated with it.
