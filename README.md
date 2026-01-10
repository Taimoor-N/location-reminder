# Location Reminder

This app allows users to create location reminders that reminds them to do something when they are at a specific location. Key functionalities:
* User Authentication: Users can signup, login and logout using the Firebase console.
* Reminders: Create new reminders, edit existing ones and also delete saved reminders.
* Map View: View user's current location on the map, select the Point of Interest and create a Geofence request with a specified radius.
* Notifications: Send reminder notifications for the saved reminders when users enters the Geofence.

## Installation
Clone the repository using the command below and import the project in Android Studio, or another IDE of your choice.\
`git clone https://github.com/Taimoor-N/location-reminder.git`

Once cloned, navigate to main app folder, create a file named "local.properties" (if it doesn't already exist) and add the following line of code there:

`MAPS_API_KEY=YOUR-API-KEY`

Replace YOUR-API-KEY with your Maps Platform API Key.
