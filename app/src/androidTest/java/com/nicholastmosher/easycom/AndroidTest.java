package com.nicholastmosher.easycom;

import android.test.AndroidTestCase;

import com.nicholastmosher.easycom.data.DataAdapter;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nick Mosher on 1/5/16.
 * @author Nick Mosher, nicholastmosher@gmail.com, github.com/nicholastmosher
 */
public class AndroidTest extends AndroidTestCase {

    @Override
    protected void runTest() throws Throwable {

        final String TEST_VALUE = "Hello, this is a data test!";

        DataAdapter<String> mStringAdapter = DataAdapter.getDataAdapter(getContext(), String.class);
        mStringAdapter.write(TEST_VALUE);

        class TestBool {public boolean completed = false;}
        final TestBool testBool = new TestBool();
        DataAdapter<String> mStringReadAdapter = DataAdapter.getDataAdapter(getContext(), String.class);
        mStringReadAdapter.read(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                assertTrue(data instanceof String);
                String stringData = (String) data;
                assertEquals(stringData, TEST_VALUE);
                testBool.completed = true;
            }
        });
        //Wait for read callback.
        while(!testBool.completed);
        System.out.println("Finished test!");
    }
}
