package com.example.notes.dao;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.notes.entitles.Notes;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class NoteDao_Impl implements NoteDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Notes> __insertionAdapterOfNotes;

  private final EntityDeletionOrUpdateAdapter<Notes> __deletionAdapterOfNotes;

  public NoteDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNotes = new EntityInsertionAdapter<Notes>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `notes` (`id`,`title`,`date_time`,`subTitle`,`note_text`,`image_path`,`color`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Notes value) {
        stmt.bindLong(1, value.getId());
        if (value.getTitle() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getTitle());
        }
        if (value.getDateTime() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getDateTime());
        }
        if (value.getSubtitle() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getSubtitle());
        }
        if (value.getNoteText() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getNoteText());
        }
        if (value.getImagePath() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getImagePath());
        }
        if (value.getColor() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getColor());
        }
      }
    };
    this.__deletionAdapterOfNotes = new EntityDeletionOrUpdateAdapter<Notes>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `notes` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Notes value) {
        stmt.bindLong(1, value.getId());
      }
    };
  }

  @Override
  public void insertNote(final Notes note) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfNotes.insert(note);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteNote(final Notes note) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfNotes.handle(note);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Notes> getAllNotes() {
    final String _sql = "SELECT * FROM notes ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
      final int _cursorIndexOfDateTime = CursorUtil.getColumnIndexOrThrow(_cursor, "date_time");
      final int _cursorIndexOfSubtitle = CursorUtil.getColumnIndexOrThrow(_cursor, "subTitle");
      final int _cursorIndexOfNoteText = CursorUtil.getColumnIndexOrThrow(_cursor, "note_text");
      final int _cursorIndexOfImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "image_path");
      final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
      final List<Notes> _result = new ArrayList<Notes>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Notes _item;
        _item = new Notes();
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpTitle;
        if (_cursor.isNull(_cursorIndexOfTitle)) {
          _tmpTitle = null;
        } else {
          _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        }
        _item.setTitle(_tmpTitle);
        final String _tmpDateTime;
        if (_cursor.isNull(_cursorIndexOfDateTime)) {
          _tmpDateTime = null;
        } else {
          _tmpDateTime = _cursor.getString(_cursorIndexOfDateTime);
        }
        _item.setDateTime(_tmpDateTime);
        final String _tmpSubtitle;
        if (_cursor.isNull(_cursorIndexOfSubtitle)) {
          _tmpSubtitle = null;
        } else {
          _tmpSubtitle = _cursor.getString(_cursorIndexOfSubtitle);
        }
        _item.setSubtitle(_tmpSubtitle);
        final String _tmpNoteText;
        if (_cursor.isNull(_cursorIndexOfNoteText)) {
          _tmpNoteText = null;
        } else {
          _tmpNoteText = _cursor.getString(_cursorIndexOfNoteText);
        }
        _item.setNoteText(_tmpNoteText);
        final String _tmpImagePath;
        if (_cursor.isNull(_cursorIndexOfImagePath)) {
          _tmpImagePath = null;
        } else {
          _tmpImagePath = _cursor.getString(_cursorIndexOfImagePath);
        }
        _item.setImagePath(_tmpImagePath);
        final String _tmpColor;
        if (_cursor.isNull(_cursorIndexOfColor)) {
          _tmpColor = null;
        } else {
          _tmpColor = _cursor.getString(_cursorIndexOfColor);
        }
        _item.setColor(_tmpColor);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
