package com.example.databasetesting;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.databasetesting.contentprovider.TestingContentProvider;
import com.example.databasetesting.database.TestingTable;

/*
 * SecondActivity allows to enter a new item 
 * or to change an existing
 */
public class SecondActivity extends Activity {
  private Spinner mCategory;
  private EditText mTitleText;
  private EditText mBodyText;

  private Uri itemUri;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.item_edit);

    mCategory = (Spinner) findViewById(R.id.category);
    mTitleText = (EditText) findViewById(R.id.item_edit_summary);
    mBodyText = (EditText) findViewById(R.id.item_edit_description);
    Button confirmButton = (Button) findViewById(R.id.item_edit_button);

    Bundle extras = getIntent().getExtras();

    // Check from the saved Instance
    itemUri = (bundle == null) ? null : (Uri) bundle
        .getParcelable(TestingContentProvider.CONTENT_ITEM_TYPE);

    // Or passed from the other activity
    if (extras != null) {
      itemUri = extras
          .getParcelable(TestingContentProvider.CONTENT_ITEM_TYPE);

      fillData(itemUri);
    }

    confirmButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        if (TextUtils.isEmpty(mTitleText.getText().toString())) {
          makeToast();
        } else {
          setResult(RESULT_OK);
          finish();
        }
      }

    });
  }

  private void fillData(Uri uri) {
    String[] projection = { TestingTable.COLUMN_SUMMARY,
        TestingTable.COLUMN_DESCRIPTION, TestingTable.COLUMN_CATEGORY };
    Cursor cursor = getContentResolver().query(uri, projection, null, null,
        null);
    if (cursor != null) {
      cursor.moveToFirst();
      String category = cursor.getString(cursor
          .getColumnIndexOrThrow(TestingTable.COLUMN_CATEGORY));

      for (int i = 0; i < mCategory.getCount(); i++) {

        String s = (String) mCategory.getItemAtPosition(i);
        if (s.equalsIgnoreCase(category)) {
          mCategory.setSelection(i);
        }
      }

      mTitleText.setText(cursor.getString(cursor
          .getColumnIndexOrThrow(TestingTable.COLUMN_SUMMARY)));
      mBodyText.setText(cursor.getString(cursor
          .getColumnIndexOrThrow(TestingTable.COLUMN_DESCRIPTION)));

      // Always close the cursor
      cursor.close();
    }
  }

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    saveState();
    outState.putParcelable(TestingContentProvider.CONTENT_ITEM_TYPE, itemUri);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  private void saveState() {
    String category = (String) mCategory.getSelectedItem();
    String summary = mTitleText.getText().toString();
    String description = mBodyText.getText().toString();

    // Only save if either summary or description
    // is available

    if (description.length() == 0 && summary.length() == 0) {
      return;
    }

    ContentValues values = new ContentValues();
    values.put(TestingTable.COLUMN_CATEGORY, category);
    values.put(TestingTable.COLUMN_SUMMARY, summary);
    values.put(TestingTable.COLUMN_DESCRIPTION, description);

    if (itemUri == null) {
      // New item
      itemUri = getContentResolver().insert(TestingContentProvider.CONTENT_URI, values);
    } else {
      // Update item
      getContentResolver().update(itemUri, values, null, null);
    }
  }

  private void makeToast() {
    Toast.makeText(SecondActivity.this, "Please maintain a summary",
        Toast.LENGTH_LONG).show();
  }
} 