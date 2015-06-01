package org.tec_hub.tecuniversalcomm.views;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nick Mosher on 5/27/15.
 */
public class TECControlView extends View {

    private List<TECControl> mControls;
    private Map<Integer, TECControl> mEventPointers;
    private boolean mEditable = false;

    public TECControlView(Context context) {
        super(context);
        mControls = new ArrayList<>();
        mEventPointers = new HashMap<>();
    }

    public void addChild(TECControl control) {
        Preconditions.checkNotNull(control);
        boolean flagDuplicate = false;
        for(TECControl tecControl : mControls) {
            if(control.conflictsWith(tecControl)) {
                new IllegalArgumentException("Control data codes cannot match!").printStackTrace();
                flagDuplicate = true;
            }
        }
        if(!flagDuplicate) {
            mControls.add(control);
        }
    }

    public void setEditableMode(boolean editable) {
        mEditable = editable;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for(TECControl tecView : mControls) {
            tecView.onDraw(canvas);
        }
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

    }

    /**
     * Multiplexes the touch events to the controls that they belong to.  A touch
     * event is assigned to a control when it is first created at ACTION_DOWN.
     * For the remainder of that event's life (i.e. until it is released by
     * ACTION_UP), it is then linked to the control which it overlapped on ACTION_DOWN.
     * The control's onTouchEvent method is then called for the remainder of the
     * event's life for handling.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean used = false;

        //For each active event pointer (i.e. unique finger click/drag/etc)
        for(int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            //If this event is a new down action
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //If this pointer is not yet mapped to a control
                if (!mEventPointers.containsKey(pointerId)) {
                    innerLoop:
                    for (TECControl control : mControls) {
                        //If this touch event overlapped a control, map it with that control
                        if (control.overlaps(event, pointerId)) {
                            mEventPointers.put(pointerId, control);
                            used = true;
                            //Only map the event with one control, the first encountered on the list
                            break innerLoop;
                        }
                    }
                }
            }

            //Pass the event to its appropriately mapped control for handling
            TECControl control = mEventPointers.get(pointerId);
            if(control != null) {
                control.onTouchEvent(event, event.findPointerIndex(pointerId), mEditable);
                this.invalidate();
            }

            //After all events are handled, address the ones that were cancelled by ACTION_UP
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(mEventPointers.containsKey(pointerId)) {
                    mEventPointers.remove(pointerId);
                    this.invalidate();
                }
            }
        }

        return used || super.onTouchEvent(event);
    }
}
