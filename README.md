ActivityLogger
==============

Android app that logs the activities of the user - WALK, ON BICYCLE, IN VEHICLE

![alt text](https://github.com/smanikandan14/ActivityLogger/blob/master/art/screen_shot1.png "")
![alt text](https://github.com/smanikandan14/ActivityLogger/blob/master/art/screen_shot2.png "")

The app logs the activities of user when the user walks, bicycles or travels in a vehicle. App uses the Play Services Activity Recognition Client to detect the user's activity and the activity logging is done automatically with a control to turn ON/OFF the
logging. Here are the design consideration details.

##IntentService vs Service
* Intially planned to implement a IntentService since it takes care of executing the code in background thread.But logic demanded to have a timer task running to identify the user state which cannot be run using IntentService as its life ends as soon as the task is finished.
* Also need to have LocationListener to get the current location if 'LastLocation' is not good enough.So the service should be active to receive the location updates.
* Decided to use a **sticky service** and use a handler thread to do heavy operations in background.

##Service
* Sticky service is used. When the service is killed or crashed, system takes care of starting back the service.
* Creates a handler thread which is used for reverseGeocoding and also in doing the big logic of when a user activity is recognized.	
* Registers for ActivityRecognition and LocationClient
* As soon as activity recognition client is connected with location services, request for activity recognition is registered with 5 secs as detection interval and pending intent 
* Receives the user activity through onStartCommand(Intent)
* When a user activity is obtained, the probable activities are considered to finalise if the detected activity is good enough to proceed. More comments can be found in the code.
* A 30 sec timer is used to determine the user's still state. 
* But in case of user in vehicle, a 2 minute timer is used to detect user still state, as user could be in traffic signal or waiting in a traffic. ( this is not as per requirement, but thought it is a good suggestion)
* Checks if there was a previous activity user was carrying out when the services is killed and restarted.

##Database
* Uses two tables, [activity & location ]
* Activity table holds all the activities made by user. The location is referenced from location table.
```
     [ id, startlocation, endlocation startTime, endTime]
     startlocation & endlocation -> foreign key to location table.
```     
* Location table stores all the start and end location of a activity. 
```
     [ id. latitude, longitude, address]
```
##UI
* Roboto different fonts are used to get the same effect as mockup
	* Bold, Italic, Medium
* Fragment is used to show Switch ( toggle On/Off), ListView	
* AddressView
	* A custom view is implemented to show start and end address. It needed a 
	'>' arrow image to be inserted betweek start address and end address.
	* Custom view takes care drawing start address, arrow bitmap, then end address.
	* Single view also avoids additional view hierarchies and improves listview scrolling
* Activity logging ON 
	* startService(ActivityLoggerService);
* Activity logging OFF
	* stopService(ActivityLoggerService);

##Loaders
* To avoid blocking the UI, AsyncTaskLoaders is used to load activities logs from database asynchronously in background thread.
* Loads all the activities from current date to from starting.
 `[Loading new detected activity could be improved by loading only the newly detected rather all activities]`
* Registers for new activity available receiver.

##Receivers
* BOOT_COMPLETED 
	* Listens for boot completed event and decides to start the ActivityLoggerService.
* New Activity available
	* Listens for new activity available message and asks the loader to force load the data.

##StickyHeaderListView
* Decided to use Sticky Headers list view to show the date for which activites are shown( just like in whatsapp chat conversation).
* Used this below library.
	* https://github.com/emilsjolander/StickyListHeaders

##Download
* The source code of demo app is available for you to play around and the app itself is available for download from play store :

<a href="https://play.google.com/store/apps/details?id=com.mani.activitylogger">
  <img alt="Get it on Google Play"
       src="https://developer.android.com/images/brand/en_app_rgb_wo_60.png" />
</a>


