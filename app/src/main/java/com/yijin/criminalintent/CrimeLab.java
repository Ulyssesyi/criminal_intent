package com.yijin.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yijin.criminalintent.database.CrimeBaseHelper;
import com.yijin.criminalintent.database.CrimeCursorWrapper;
import com.yijin.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext)
                .getWritableDatabase();
    }

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(CrimeTable.Cols.UUID + " = ?", new String[] { id.toString() });
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);

        mDatabase.insert(CrimeTable.Name, null, values);
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.Name, values, CrimeTable.Cols.UUID + " = ?", new String[] {uuidString});
    }

    public CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.Name,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
                );
        return new CrimeCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CrimeTable.Cols.UUID, crime.getId().toString());
        contentValues.put(CrimeTable.Cols.TITLE, crime.getTitle());
        contentValues.put(CrimeTable.Cols.DATE, crime.getDate().toString());
        contentValues.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        return contentValues;
    }
}
