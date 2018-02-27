# Everyday

## Contents
* [Download the app](#download-the-app)
* [User documentation](#user-documentation)
* [Developer documentation](#developer-documentation)
* [Testing documentation](#testing-documentation)

![alt text](https://github.com/lesswifi/Everyday/blob/master/1.png)
![alt text](https://github.com/lesswifi/Everyday/blob/master/2.png)

## Download the app
 Clone or download this git repo. Open the project in Android Studio. Connect your Android device to your computer, and run the app.

## User documentation
### Making an account
When you open Everyday for the first time, get started by logging into your Google account.

### Creating an entry
On the home page, press the `+` action button at the bottom right. When the editor comes up, simply fill in a title and write about whatever's on your mind. If you want to discard changes, go `back` and don't save.

When you're satisfied with your entry, save it by pressing the check mark action button at the bottom right. The location and weather will be saved. The content of your entry will also be analyzed by IBM's Watson natural language processing API to assign you a sentiment score, which measures how positive or negative the tone of your entry is.

#### Adding a tag
Press the `#tag` under the title to bring up a list of your tags. Select a tag that best fits your note. To see how to customize your tags, go to [Tags](#tags).

#### Adding photos
Press the camera icon on the toolbar in the top left corner. Take as many pictures as you want to remember the moment. You should see your photos show up on the page.

If it's your first time doing this, the app will ask for permission to use the camera. Once you grant permission, re-navigate to the camera. The app will remember this and won't ask for permission again.

#### Deleting photos
Press and hold the photo you want to get rid of until you see a delete prompt. Select `OK` to delete. If you delete a photo, it cannot be undone! Photos you save on Everyday are stored only on the app, not on your phone's gallery.

#### Adding audio
Press the mic icon on the toolbar in the top left corner. A popup will prompt you to start recording. Select `Start` to begin recording and `End` to stop. You should now be able to playback your audio or record over it.

If it's your first time doing this, the app will ask for permission. Once you grant permission, re-navigate to the audio recorder. The app will remember this and won't ask for permission again.

#### Deleting audio
Press and hold the audio playback until you see a delete prompt. Select `OK` to delete. If you delete audio, it cannot be undone! Audio you save on Everyday is stored only on the app, not on your phone.

### Tags
Select the `menu` button in the top right and select `Tags`. Here you can see a list of your tags and how many entries you have associated with each one. Press on a tag to see a list of entries tagged with it.

#### Editing tags
Click the `pencil` button on a tag to change its name. This will change the tag name of all entries tagged with this.

#### Adding tags
Click the `+` action button at the bottom right to add a new tag.

#### Deleting tags
Click the `x` to delete a tag. This will remove the tag from all entries tagged with this.

### Atlas
Select the `menu` button in the top right and select `Atlas`. Here you can see your entries on a map.

### Analytics
Select the `menu` button in the top right and select `Analytics`. Here you can see your entries plotted over time based on their sentiment scores. Click a point on the plot to see the note it's associated with.

### Logging out
Select `Logout` from the sidebar menu.

### Deleting your account
Select `Delete account!` at the bottom of the sidebar menu.

## Developer documentation
Refer to our in-code comments for detailed explanations. Below are some helpful starting points for functionality you might be interested in.

### Authentication
See `Authentication/AuthUiActivity`.

### Journal entries

#### Fields
All fields in a journal entry are defined in `Models/JournalEntry`.

### List of entries
`MainActivity` calls `Journal/JournalListFragment`, which sets up a RecyclerView of `Journal/JournalViewHolder`s. Each journal entry preview is displayed with layout from `layout/row_note_list.xml`.

#### Add/create a new entry
When prompted to view an entry, the app opens `Journal/AddJournalActivity`, which creates `Journal/JournalEditorFragment`. This fragment handles all editing and previewing of title, content, images, audio, and asking permission for images and audio.

#### Saving an entry
This all takes place in `Journal/JournalEditorFragment`.
When the save button is clicked, `validateAndSaveContent()` is called, which starts an AsyncTask that runs `addNoteToFirebase()` in the background. This ensures that photos and audios can completely be uploaded to Firebase storage and their URLs saved before the journal entry is updated. In `addNoteToFirebase()`, all content is updated, photos and audio are uploaded and saved, and NLP analysis is done.

### Tags

#### Fields
All fields in a journal entry are defined in `Models/Tag`.

### List of tags
`Tag/TagListActivity` opens `Tag/TagListFragment` which lists the tags using `Tag/TagListAdaptor`.

### Edit tags
See `Tag/AddTagFragment`, `Tag/SelectTagFragment` and `Tag/TagDialogAdaptor`.

### Analytics
`AnalyticsActivity` gets journal entries from Firebase and displays their sentiment scores on an XYPlot.

### Atlas
`AtlasActivity` gets journal entries from Firebase and displays them on a map using the Google Maps API.

## Testing Documentation

### Activity Behavior Testing

Each Activity was systematically tested on its behavior when rotated, interrupted, or restarted. The navigation between all activities was tested through the navigation drawer, as well as through the action buttons on each activity page.

### Usability Testing

We ensured that all activities had a simple and appealing UI such that there would be no confusion on how to navigate through the app. We accomplished this by have a navigation drawer available throughout each activity, as well as an action icon on the bottom right of the screen.

### Authentication Testing

Testing was done to make sure users can log in and log out, show previous logged in accounts, and stay logged in until they log out. We also allow users to delete their account, which deletes all the data stored for that user through the app.

### Permission Testing

Everyday requires the following permissions/settings from the user:

* Location Services On
* Location Permissions
* Image Capture Permissions
* Audio Record Permissions

The app prompts the user to make a choice as to whether they would like to accept or deny these permissions if that current user has not accepted permission before, and the app behaves normally on both conditions.

### Multiple Devices

When a user is logged into multiple devices, the default set up of the app is the same as if they were to only be logged into one device.

### Firebase Testing

We ensured that changing categories (a shared attribute among journal entries) changes the individual journal entry attribute values.
