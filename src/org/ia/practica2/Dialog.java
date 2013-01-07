package org.ia.practica2;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Dialog extends DialogFragment {

	private String mText;

	public Dialog(String text) {

		mText = text;
	}

	@TargetApi(11)
	@Override
	public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {

		/** Fill the Dialog with the path on a text format */

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_layout, null);

		TextView tv = (TextView) layout.findViewById(R.id.Dialog_text);
		tv.setText(mText);

		builder.setView(layout);
		builder.setMessage("").setPositiveButton("Aceptar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		return builder.create();
	}
}
