package android.content.pm;
oneway interface IPackageDeleteObserver2 {
    void onUserActionRequired(in Intent intent);
    void onPackageDeleted(String packageName, int returnCode, String msg);
}
