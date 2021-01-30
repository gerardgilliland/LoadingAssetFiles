# LoadingAssetFiles
The sole purpose of this app is to learn the process of Loading Asset Files from the Google Play store.
IT DOES NOT WORK AT THIS TIME.

I have a large, complex, app that can not download the Asset Files.
It is running as an .apk file. Google Play requires .aab files by August, 2021.
I have converted it to an .aab (app bundle) file but can not download the Asset Files.
This app is copy of the large app and all functionality has been removed other than Loading Asset Files.
Instead of loading five Asset Files each containing 40 to over 100 files in each group,
I am loading two Asset Files each containing only 3 files.
I do not know if the problem is in Android Studio, Google Play, and/or (most likely) my code.
But this app is the size I can work with.

The Asset Files contain bird songs.
There are NO songs in this app. You have to load them from an Asset Pack.
There are currently two asset packs that are associated with this app: New World, Old World.

To import the songs:
1) Install this Loading Asset Files app.
2) In this app tap the Path button.
3) On the Path screen select New World, or Old World.
4) It opens a new screen Load Asset Pack where you should be able to view the status of the Asset Pack downloading.
(The function loadOnePack is disabled. It crashes the app.) 
5) Return to the Path screen and tap the "Load Now" button.
6) This should transfer the Asset Files to a common location.
7) Click the Songs button to verify the songs loaded.

The source code is at GitHub 
https://github.com/gerardgilliland/LoadingAssetFiles
It has one song in the app/assets/Song folder that you can copy to your phones Download folder.
It can then optionally be transferred to your Songs list from the Path Screen. 

The Help button brings up what you are reading now. 

This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
