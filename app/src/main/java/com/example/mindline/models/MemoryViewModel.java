package com.example.mindline.models;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mindline.database.MemoryDao;
import com.example.mindline.database.AppDatabase;
import com.example.mindline.models.Memory;

import java.util.List;

public class MemoryViewModel extends AndroidViewModel {

    private final MemoryDao memoryDao;
    private final LiveData<List<Memory>> allMemories;

    public MemoryViewModel(@NonNull Application application) {
        super(application);
        memoryDao = AppDatabase.getInstance(application).memoryDao();
        allMemories = memoryDao.getAllMemories();
    }

    public LiveData<List<Memory>> getAllMemories() {
        return allMemories;
    }

    public void insert(final Memory memory) {
        AppDatabase.databaseWriteExecutor.execute(() -> memoryDao.insert(memory));
    }
    public LiveData<Memory> getMemoryById(long id) {
        return memoryDao.getMemoryById(id);
    }

    public int updateMemory(Memory memory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            memoryDao.updateMemory(memory);
        });
        return 1;
    }

    public void deleteMemoryById(long id) {
        AppDatabase.databaseWriteExecutor.execute(() -> memoryDao.deleteMemoryById(id));
    }

    public LiveData<List<Memory>> searchMemories(String query) {
        return memoryDao.searchMemories("%" + query + "%");
    }

    public LiveData<List<Memory>> getMemoriesSince(long timestamp) {
        return memoryDao.getMemoriesSince(timestamp);
    }




    // Add other methods to interact with the database if needed
}

