package com.example.mindline.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.mindline.models.Memory;
import java.util.List;

@Dao
public interface MemoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Memory memory);

    @Update
    void update(Memory memory);

    @Delete
    void delete(Memory memory);

    @Query("SELECT * FROM memory")
    LiveData<List<Memory>> getAllMemories();

    @Query("SELECT * FROM memory WHERE date BETWEEN :startDate AND :endDate")
    LiveData<List<Memory>> getMemoriesByDateRange(String startDate, String endDate);

//    @Query("SELECT * FROM memory WHERE title LIKE :query OR description LIKE :query")
//    LiveData<List<Memory>> searchMemories(String query);

    @Query("SELECT * FROM memory WHERE id = :id")
    LiveData<Memory> getMemoryById(long id);

    @Update
    void updateMemory(Memory memory);

    @Query("DELETE FROM memory WHERE id = :id")
    void deleteMemoryById(long id);


    @Query("SELECT * FROM memory WHERE UPPER(title) LIKE UPPER(:query) OR UPPER(description) LIKE UPPER(:query) ORDER BY date DESC")
    LiveData<List<Memory>> searchMemories(String query);

    @Query("SELECT * FROM Memory WHERE date >= :timestamp ORDER BY date DESC")
    LiveData<List<Memory>> getMemoriesSince(long timestamp);
}

