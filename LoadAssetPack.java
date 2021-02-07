package com.modelsw.loadingassetfiles;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.play.core.assetpacks.AssetPackState;
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener;
import com.google.android.play.core.assetpacks.model.AssetPackStatus;
import com.google.android.play.core.tasks.Task;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

// SECTION 1 -- INITIALIZE
public class LoadAssetPack extends AppCompatActivity {
    private static final String TAG = "LoadAssetPack";
    private TextView packName;
    private TextView packLocation;
    private TextView packCompletion;
    String apack = Main.assetPackName;
    private Button back;
    private final int junk = 0;
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.load_asset_pack);
        String msg = "Here in loadAsssetPack() onCreate Main.assetPackName: " + apack;
        Log.i(TAG, msg);

        // action bar toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setLogo(R.drawable.treble_clef_linen);
        toolbar.setTitleTextColor(getResources().getColor(R.color.teal));
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Navigation Icon tapped");
                finish();
            }
        });

        packName = findViewById(R.id.pack_name);
        packName.setText(apack);
        packLocation = findViewById(R.id.pack_location);
        packLocation.setText("xxxx");
        packCompletion = findViewById(R.id.pack_completion);
        packCompletion.setText("  0 %");

        Log.i(TAG, "display asset pack on screen: " + apack);
        back = findViewById(R.id.return_button);

        back.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Returning from Load Asset Pack");
                finish();
            }
        });


        Log.i(TAG, "before call loadOnePack");
        loadOnePack();
        Log.i(TAG, "after call loadOnePack");

    }

    // flow diagram
    // https://developer.android.com/guide/playcore/asset-delivery/integrate-java

    // SECTION 2
    public void loadOnePack() {
        long totalSize = 0;
        Log.i(TAG, "loadOnePack: " + apack);

        try {
            Log.i(TAG, "before clearListeners");
            assetPackManager.clearListeners();
            Log.i(TAG, "after clearListeners");
        } catch (Exception e) {
            Log.i(TAG, "catch clearListeners error: " + e);
            e.printStackTrace();
            finish();
        }

        try {
            Log.i(TAG, "before getPackLocation apack:" + apack);
            String ap = assetPackLocation.getPackLocation(apack);
            Log.i(TAG, "after getPackLocation ap: " + ap);
            packLocation = findViewById(R.id.pack_location);
            packLocation.setText(ap);
            Log.i(TAG, "after setText on view");
            Main.assetPackLocation = ap;
            Log.i(TAG, "after set Main.assetPackLocation: " + Main.assetPackLocation);
        } catch (Exception e) {
            Log.i(TAG, "catch assetPackLocation error: " + e);
            e.printStackTrace();
            finish();
        }

        try {
            Log.i(TAG, "before getPackStates apack:" + apack);
            assetPackStates.getPackStates(Collections.singletonList(apack));
            Log.i(TAG, "after getPackStates assetPackStates:" + assetPackStates);
        } catch (Exception e) {
            Log.i(TAG, "catch assetPackStates error: " + e);
            e.printStackTrace();
            finish();
        }

        assetPackStateUpdateListener = new AssetPackStateUpdateListener() {
            void registerListener() {
                Log.i(TAG, "registerListener");
            };

            @Override
            public void onStateUpdate(AssetPackState assetPackState) {
                switch (assetPackState.status()) {
                    case AssetPackStatus.PENDING:
                        Log.i(TAG, "AssetPackStatus PENDING");
                        break;

                    case AssetPackStatus.DOWNLOADING:
                        long downloaded = assetPackState.bytesDownloaded();
                        long totalSize = assetPackState.totalBytesToDownload();
                        double percent = 100.0 * downloaded / totalSize;
                        //percentCompletion = findViewById(R.id.percent_completion);
                        Log.i(TAG, "PercentDone=" + String.format("%.2f", percent));
                        break;

                    case AssetPackStatus.TRANSFERRING:
                        // 100% downloaded and assets are being transferred.
                        // Notify user to wait until transfer is complete.
                        Log.i(TAG, "AssetPackStatus TRANSFERRING");
                        break;

                    case AssetPackStatus.COMPLETED:
                        // Asset pack is ready to use. Start the game.
                        Main.assetPackLoaded = true;
                        Log.i(TAG, "AssetPackStatus COMPLETED");
                        break;

                    case AssetPackStatus.FAILED:
                        // Request failed. Notify user.
                        Log.i(TAG, "AssetPackStatus FAILED");
                        break;

                    case AssetPackStatus.CANCELED:
                        // Request canceled. Notify user.
                        Log.i(TAG, "AssetPackStatus CANCELED");
                        break;

                    case AssetPackStatus.NOT_INSTALLED:
                        // Asset pack is not downloaded yet.
                        Log.i(TAG, "AssetPackStatus NOT_INSTALLED");
                        break;
                }
            }
        };


        long downloaded = assetPackState.bytesDownloaded();
        double percent = 100.0 * downloaded / totalSize;
        Log.i(TAG, "PercentDone=" + String.format("%.2f", percent));

    } // loadOnePack

    // SECTION 3 -- MY INTERPRETATION OF ABSTRACT INTERFACE IN SECTION 4 -- CALLED FROM loadOnePack (SECTION 2) ABOVE

    // https://developer.android.com/reference/com/google/android/play/core/assetpacks/AssetPackManager#getpacklocation
    // Manages downloads of asset packs.

    AssetPackManager assetPackManager = new AssetPackManager() {
        // Unregisters all listeners previously added using registerListener(AssetPackStateUpdateListener).
        @Override
        public void clearListeners() {

        }
    };

    Map<String, AssetPackManager.AssetPackState> packStates() {
        AssetPackManager.AssetPackState assetPackState = packStates().get(apack);
        return assetPackState;
    }

    AssetPackManager.AssetLocation assetLocation = new AssetPackManager.AssetLocation() {

        long offset() {
            return offset(); // Returns the file offset where the asset starts, in bytes.
        }

        String path() {
            return path(); // Returns the path to the file containing the asset.
        }

        long size() {
            return size(); // Returns the size of the asset, in bytes.
        }

        void getAssetLocation(String packName, String assetPath) {
            getAssetLocation(packName, assetPath);
        }

    };

    AssetPackStateUpdateListener assetPackStateUpdateListener = new AssetPackStateUpdateListener() {
        @Override
        public void onStateUpdate(AssetPackState state) {

        }

        void registerListener(AssetPackStateUpdateListener listener) {

        };

        void unregisterListener(AssetPackStateUpdateListener listerner) {

        };

        void clearListeners() {

        };

    };


    // https://developer.android.com/reference/com/google/android/play/core/assetpacks/AssetPackStates.html#AssetPackStates()
    AssetPackManager.AssetPackStates assetPackStates = new AssetPackManager.AssetPackStates() {

        // Returns a map from a pack's name to its state.
        // collection interface Map<Key, Value>
        // requires import java.util.Map
        //Returns total size of all requested packs in bytes.
        //long totalBytes();


        @Override
        Task<AssetPackManager.AssetPackStates> fetch(List<String> packNames) {
            return null;
        }

        @Override
        AssetPackManager.AssetPackStates cancel(List<String> packNames) {
            return null;
        }

        @Override
        Task<AssetPackManager.AssetPackStates> getPackStates(List<String> packNames) {
            Task<AssetPackManager.AssetPackStates> aps = getPackStates(packNames);
            Log.i(TAG, "AssetPackStates aps: " + aps);
            return aps;
        }

        @Override
        Task<Void> removePack(String packName) {
            return null;
        }
    };



    AssetPackManager.AssetPackLocation assetPackLocation = new AssetPackManager.AssetPackLocation() {

        final AssetPackManager.AssetPackStorageMethod assetPackStorageMethod = new AssetPackManager.AssetPackStorageMethod() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            final int	STORAGE_FILES = 0;
            int packStorageMethod () {
                return STORAGE_FILES;
            }

        };

        @Override
        public String path() {
            return null;
        }

        @Override
        public String assetsPath() {
            return null;
        }

        @Override
        String getPackLocation(String packName) {
            return getPackLocation(packName);
        }

        @Override
        Map<String, AssetPackManager.AssetPackLocation> getPackLocations() {
            return null;
        }

    };

    AssetPackManager.AssetPackState assetPackState = new AssetPackManager.AssetPackState() {

        @Override
        public String name() {
            return null;
        }

        @Override
        public int status() {
            return 0;
        }

        @Override
        public int errorCode() {
            return 0;
        }

        @Override
        public long bytesDownloaded() {
            return 0;
        }

        @Override
        public long totalBytesToDownload() {
            return 0;
        }

        @Override
        public int transferProgressPercentage() {
            return 0;
        }

        public int a() {
            return 0;
        }


    };

    void onStateUpdate(AssetPackManager.AssetPackState assetPackState) {
        switch (assetPackState.status()) {
            case AssetPackStatus.PENDING:
                Log.i(TAG, "Pending");
                break;
            case AssetPackStatus.DOWNLOADING:
                long downloaded = assetPackState.bytesDownloaded();
                long totalSize = assetPackState.totalBytesToDownload();
                double percent = 100.0 * downloaded / totalSize;
                Log.i(TAG, "PercentDone=" + String.format("%.2f", percent));
                break;
            case AssetPackStatus.TRANSFERRING:
                // 100% downloaded and assets are being transferred.
                // Notify user to wait until transfer is complete.
                break;
            case AssetPackStatus.COMPLETED:
                // Asset pack is ready to use. Start the game.
                break;
            case AssetPackStatus.FAILED:
                // Request failed. Notify user.
                Log.e(TAG, String.valueOf(assetPackState.errorCode()));
                break;
            case AssetPackStatus.CANCELED:
                // Request canceled. Notify user.
                break;
                    /*
                    I do NOT control Wi-Fi vs Cellular. You decide.
                    But recognize Cellular can incur large costs when downloading Songs.
                    case AssetPackStatus.WAITING_FOR_WIFI:
                        if (!waitForWifiConfirmationShown) {
                            assetPackManager.showCellularDataConfirmation(this)
                                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                                        @Override
                                        public void onSuccess(Integer resultCode) {
                                            if (resultCode == RESULT_OK) {
                                                Log.d(TAG, "Confirmation dialog has been accepted.");
                                            } else if (resultCode == RESULT_CANCELED) {
                                                Log.d(TAG, "Confirmation dialog has been denied by the user.");
                                            }
                                        }
                                    });
                            //waitForWifiConfirmationShown = true;
                        }
                        break;
                    */
            case AssetPackStatus.NOT_INSTALLED:
                // Asset pack is not downloaded yet.
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + assetPackState.status());
        } // switch
    } // onStateUpdate


// SECTION 4 -- INTERFACE AND ABSTRACT CLASSES AND METHODS -- NOT USED BUT INTERPETED ABOVE IN SECTION 3

    interface AssetPackManager {


        abstract class AssetPackStates extends Object {
            public AssetPackStates() { // Constructor
                super();
            }

            Map<String, AssetPackState> packStates() {
                return packStates();
            }

            long totalBytes() {
                return totalBytes();
            }

            abstract Task<AssetPackStates> fetch(List<String> packNames);

            abstract AssetPackStates cancel(List<String> packNames);

            abstract Task<AssetPackStates> getPackStates(List<String> packNames);

            abstract Task<Void> removePack(String packName);


        }

        void clearListeners();

        interface AssetPackStateUpdateListener {
            void registerListener(AssetPackStateUpdateListener listener);

            void unregisterListener(AssetPackStateUpdateListener listener);

            void clearListeners();

        }

        abstract class AssetPackState extends Object implements Map<String, AssetPackState> {
            AssetPackState() {  // Constructor

            }

            abstract long bytesDownloaded();

            abstract int errorCode();

            abstract String name();

            abstract int status();

            abstract long totalBytesToDownload();

            public abstract int transferProgressPercentage();

            public void onStateUpdate(AssetPackState assetPackState) {
                switch (assetPackState.status()) {
                    case AssetPackStatus.PENDING:
                        Log.i(TAG, "Pending");
                        break;
                    case AssetPackStatus.DOWNLOADING:
                        long downloaded = assetPackState.bytesDownloaded();
                        long totalSize = assetPackState.totalBytesToDownload();
                        double percent = 100.0 * downloaded / totalSize;
                        Log.i(TAG, "PercentDone=" + String.format("%.2f", percent));
                        break;
                    case AssetPackStatus.TRANSFERRING:
                        // 100% downloaded and assets are being transferred.
                        // Notify user to wait until transfer is complete.
                        break;
                    case AssetPackStatus.COMPLETED:
                        // Asset pack is ready to use. Start the game.
                        break;
                    case AssetPackStatus.FAILED:
                        // Request failed. Notify user.
                        Log.e(TAG, String.valueOf(assetPackState.errorCode()));
                        break;
                    case AssetPackStatus.CANCELED:
                        // Request canceled. Notify user.
                        break;
                    /*
                    I do NOT control Wi-Fi vs Cellular. You decide.
                    But recognize Cellular can incur large costs when downloading Songs.
                    case AssetPackStatus.WAITING_FOR_WIFI:
                        if (!waitForWifiConfirmationShown) {
                            assetPackManager.showCellularDataConfirmation(this)
                                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                                        @Override
                                        public void onSuccess(Integer resultCode) {
                                            if (resultCode == RESULT_OK) {
                                                Log.d(TAG, "Confirmation dialog has been accepted.");
                                            } else if (resultCode == RESULT_CANCELED) {
                                                Log.d(TAG, "Confirmation dialog has been denied by the user.");
                                            }
                                        }
                                    });
                            //waitForWifiConfirmationShown = true;
                        }
                        break;
                    */
                    case AssetPackStatus.NOT_INSTALLED:
                        // Asset pack is not downloaded yet.
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + assetPackState.status());
                } // switch
            } // onStateUpdate

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(@Nullable Object key) {
                return false;
            }

            @Override
            public boolean containsValue(@Nullable Object value) {
                return false;
            }

            @Nullable
            @Override
            public AssetPackState get(@Nullable Object key) {
                return null;
            }

            @Nullable
            @Override
            public AssetPackState put(String key, AssetPackState value) {
                return null;
            }

            @Nullable
            @Override
            public AssetPackState remove(@Nullable Object key) {
                return null;
            }

            @Override
            public void putAll(@NonNull Map<? extends String, ? extends AssetPackState> m) {

            }

            @Override
            public void clear() {

            }

            @NonNull
            @Override
            public Set<String> keySet() {
                return null;
            }

            @NonNull
            @Override
            public Collection<AssetPackState> values() {
                return null;
            }

            @NonNull
            @Override
            public Set<Entry<String, AssetPackState>> entrySet() {
                return null;
            }
        }



        abstract class AssetPackLocation extends Object {
            AssetPackLocation() { // Constructor
            }

            String assetsPath() { // folder containing the pack's assets
                return assetsPath();
            }

            String path() { // folder containing the extracted asset pack
                return path();
            }

            String getPackLocation(String packName) {
                return getPackLocation(packName);
            }

            abstract Map<String, AssetPackLocation> getPackLocations();

        }

        @interface AssetPackStorageMethod {
            int	STORAGE_FILES = 0;
        }

        abstract class AssetLocation extends Object {
            AssetLocation() { // Constructor
            }

            long offset() {
                return offset(); // Returns the file offset where the asset starts, in bytes.
            }

            String path() {
                return path(); // Returns the path to the file containing the asset.
            }

            long size() {
                return size(); // Returns the size of the asset, in bytes.
            }

            void getAssetLocation(String packName, String assetPath) {
                getAssetLocation(packName, assetPath);
            }
        }


    }

}

