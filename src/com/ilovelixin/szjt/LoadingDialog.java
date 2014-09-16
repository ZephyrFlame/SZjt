package com.ilovelixin.szjt;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoadingDialog
{
    public static Dialog createLoadingDialog(Context context, String msg)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);

        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);

        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context, R.drawable.loading_animation);
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);

        loadingDialog.setCancelable(false);
        loadingDialog.setContentView(layout);

        return loadingDialog;
    }
}
