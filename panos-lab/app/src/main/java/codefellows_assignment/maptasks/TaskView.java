package codefellows_assignment.maptasks;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskView extends AppCompatActivity implements ValueEventListener{
    @BindView(R.id.taskList)
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);

        ButterKnife.bind(this);

        DatabaseReference tasks = FirebaseDatabase.getInstance().getReference("tasks");
        tasks.addValueEventListener(this);

        linearLayoutManager = new LinearLayoutManager(this);
        taskAdapter = new TaskAdapter();

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(taskAdapter);
    }

    @Override
    public void onDataChange(DataSnapshot ds){
        List<Task> tasks = new ArrayList<>();
        for(DataSnapshot snapshot : ds.getChildren()){
            tasks.add(Task.fromSnapshot(snapshot));
        }
        taskAdapter.tasks = tasks;
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError dbError){}


}
