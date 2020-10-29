package org.coralibre.android.sdk.internal.database.persistent;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.coralibre.android.sdk.internal.database.persistent.entity.EntityDiagnosisKey;

import java.util.List;

@Dao
public interface DaoDiagnosisKey {

    // TODO what happens on update?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public void insertDiagnosisKeys(List<EntityDiagnosisKey> entityDiagnosisKeys);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public void updateDiagnosisKeys(List<EntityDiagnosisKey> entityDiagnosisKeys);

    @Query("SELECT * FROM EntityDiagnosisKey WHERE tokenString = :token")
    public List<EntityDiagnosisKey> getDiagnosisKeys(String token);

}
