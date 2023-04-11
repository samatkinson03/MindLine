package com.example.mindline.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.mindline.database.MemoryDao;
import com.example.mindline.database.AppDatabase;
import com.example.mindline.models.Memory;
import java.util.List;

public class MemoryViewModel extends AndroidViewModel {

    private MemoryDao memoryDao;
    private LiveData<List<Memory>> allMemories;

    public MemoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        memoryDao = db.memoryDao();
        allMemories = memoryDao.getAllMemories();
    }

    public LiveData<List<Memory>> getAllMemories() {
        return allMemories;
    }

    public void insert(Memory memory) {
        AppDatabase.databaseWriteExecutor.execute(() -> memoryDao.insert(memory));
    }

    public void update(Memory memory) {
        AppDatabase.databaseWriteExecutor.execute(() -> memoryDao.update(memory));
    }

    public void delete(Memory memory) {
        AppDatabase.databaseWriteExecutor.execute(() -> memoryDao.delete(memory));
    }
    public LiveData<List<Memory>> searchMemories(String query) {
        return memoryDao.searchMemories(query);
    }

    public LiveData<Memory> getMemoryById(long id) {
        // Replace 'yourMemoryRepository' with the actual name of your memory repository variable
        return memoryDao.getMemoryById(id);
    }

    public void deleteMemoryById(long memoryId) {
        // Implement the deletion of the memory with the given ID
    }

    public void updateMemory(Memory memory) {
        // Replace 'yourMemoryRepository' with the actual name of your memory repository variable
        memoryDao.updateMemory(memory);
    }
}
