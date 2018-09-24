package codefellows_assignment.maptasks;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

public class Task {
    String id;
    String description;
    LatLng start;
    LatLng end;
    boolean isDone;

    public Task(){}

    public Task(String s1, LatLng ll1, LatLng ll2){
        this.id = "";
        this.description = s1;
        this.start = ll1;
        this.end = ll2;
    }

    public static Task fromSnapshot(DataSnapshot snapshot){
        Task task = new Task();

        task.id = snapshot.getKey();
        task.description = snapshot.child("description").getValue(String.class);
        task.isDone = snapshot.child("isDone").getValue(boolean.class);

        float startLat = snapshot.child("start").child("lat").getValue(float.class);
        float startLong = snapshot.child("start").child("long").getValue(float.class);
        task.start = new LatLng(startLat,startLong);

        float endLat = snapshot.child("end").child("lat").getValue(float.class);
        float endLong = snapshot.child("end").child("long").getValue(float.class);
        task.end = new LatLng(endLat,endLong);

        return task;
    }

}
