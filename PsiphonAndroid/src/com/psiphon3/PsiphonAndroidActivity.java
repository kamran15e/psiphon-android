/*
 * Copyright (c) 2012, Psiphon Inc.
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.psiphon3;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import ch.ethz.ssh2.*;
import java.io.IOException;

public class PsiphonAndroidActivity extends Activity 
{
    private TableLayout messagesTableLayout;
    private ScrollView messagesScrollView;
    private Animation animRotate;
    private ImageView startImageView;
    private Thread tunnelThread;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.messagesTableLayout = (TableLayout)findViewById(R.id.messagesTableLayout);
        this.messagesScrollView = (ScrollView)findViewById(R.id.messagesScrollView);
        this.startImageView = (ImageView)findViewById(R.id.startImageView);
        this.animRotate = AnimationUtils.loadAnimation(this, R.anim.rotate);

        /*
        tunnelThread = new Thread(new Runnable()
        {
            public void run()
            {
                testTunnel();
            }
        });

        tunnelThread.start();
        */
        
        AddMessage("onCreate finished", MessageClass.DEBUG);

        this.startImageView.post(new Runnable() {
            @Override
            public void run() {
                spinImage();
            }
        });
    }
    
    public enum MessageClass { GOOD, BAD, NEUTRAL, DEBUG };
    
    public void AddMessage(String message, MessageClass messageClass)
    {
        TableRow row = new TableRow(this);
        TextView messageTextView = new TextView(this);
        ImageView messageClassImageView = new ImageView(this);
        
        messageTextView.setText(message);
        
        int messageClassImageRes = 0;
        int messageClassImageDesc = 0;
        switch (messageClass)
        {
        case GOOD:
            messageClassImageRes = android.R.drawable.presence_online;
            messageClassImageDesc = R.string.message_image_good_desc;
            break;
        case BAD:
            messageClassImageRes = android.R.drawable.presence_busy;
            messageClassImageDesc = R.string.message_image_bad_desc;
            break;
        case DEBUG:
            messageClassImageRes = android.R.drawable.presence_away;
            messageClassImageDesc = R.string.message_image_debug_desc;
            break;
        default:
            messageClassImageRes = android.R.drawable.presence_invisible;
            messageClassImageDesc = R.string.message_image_neutral_desc;
            break;
        }
        messageClassImageView.setImageResource(messageClassImageRes);
        messageClassImageView.setContentDescription(getResources().getText(messageClassImageDesc));
        
        row.addView(messageTextView);
        row.addView(messageClassImageView);
        
        this.messagesTableLayout.addView(row);
        
        this.messagesScrollView.post(new Runnable() {
            @Override
            public void run() {
                messagesScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    
    private void spinImage()
    {
        startImageView.startAnimation(animRotate);
    }

    public void testTunnel()
    {
        String hostname = "...";
        int port = 22;
        String username = "...";
        String password = "...";
        String obfuscationKeyword = "...";

        try
        {
            Connection conn = new Connection(hostname, obfuscationKeyword, port);
            conn.connect();
            Log.d("Psiphon", "SSH connected");

            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated == false)
            {
                Log.e("Psiphon", "can't authenticate");
                return;
            }
            Log.d("Psiphon", "SSH authenticated");

            DynamicPortForwarder socks = conn.createDynamicPortForwarder(1080);
            Log.d("Psiphon", "SOCKS running");

            try
            {
                Thread.sleep(60000);
            }
            catch (InterruptedException e)
            {
            }            

            socks.close();
            conn.close();
            Log.d("Psiphon", "SSH/SOCKS closed");
        }
        catch (IOException e)
        {
            Log.e("Psiphon", "IOException", e);
            return;
        }
    }
}
