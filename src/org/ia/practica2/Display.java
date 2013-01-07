package org.ia.practica2;

import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class Display extends Activity {

	private Spinner OrigenSpinner;
	private Spinner DestiSpinner;
	private Astar AstarTask;
	private Vector<Integer> ruta;
	private int Origen;
	private int Desti;
	private Vector<String> mNoms = new Vector<String>();
	private String[] mNomsString = { "Renfe", "Eureka", "CC.Educacio - FTI",
			"Ciencies i Biociencies", "Eix central",
			"Escola de Postgrau - FGC", "Enginyeria - SAF",
			"FGC - Eix central", "Lletres i Psicologia",
			"Medicina - CC.Comunicacio", "Rectorat - Veterinaria", "Vila nord",
			"Vila sud" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_layout);

		/**
		 * Populate the spinners from an String array and set their respectives
		 * adapters
		 */
		OrigenSpinner = (Spinner) findViewById(R.id.origen_spinner);
		ArrayAdapter<CharSequence> OrigenAdapter = ArrayAdapter
				.createFromResource(this, R.array.estacions_array,
						android.R.layout.simple_spinner_item);
		OrigenAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		OrigenSpinner.setAdapter(OrigenAdapter);

		DestiSpinner = (Spinner) findViewById(R.id.desti_spinner);
		ArrayAdapter<CharSequence> DestiAdapter = ArrayAdapter
				.createFromResource(this, R.array.estacions_array,
						android.R.layout.simple_spinner_item);
		DestiAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		DestiSpinner.setAdapter(DestiAdapter);

		Button AcceptButton = (Button) findViewById(R.id.accept_button);
		AcceptButton.setOnClickListener(new View.OnClickListener() {

			/**
			 * Execute the A* thread with the cost and heuristic from matrix and
			 * spinner
			 */
			public void onClick(View v) {

				Origen = OrigenSpinner.getSelectedItemPosition();
				Desti = DestiSpinner.getSelectedItemPosition();
				AstarTask = new Astar();
				AstarTask.execute();

			}
		});

	}
	
	/** Class containing the A* execution on a thread (inner class used to return the data to UI) */

	public class Astar extends AsyncTask<Void, Void, Vector<Integer>> {

		/**
		 * stations
		 * 
		 * 0 - Renfe 1 - Eureka 2 - CC.Educacio - FTI 3 - Ciencies i Biociencies
		 * 4 - Eix central 5 - Escola de Postgrau - FGC 6 - Enginyeria - SAF 7 -
		 * FGC - Eix central 8 - Lletres i Psicologia 9 - Medicina -
		 * CC.Comunicacio 10 - Rectorat - Veterinaria 11 - Vila nord 12 - Vila
		 * sud
		 */

		/**
		 * Cost in minuts (including the UAB 5 lines) from one station to the next
		 * (0 => no destination)
		 */

		private Vector<Vector<Integer>> mRuta = new Vector<Vector<Integer>>();
		private Vector<Integer> C = new Vector<Integer>();
		private Vector<Integer> E = new Vector<Integer>();

		private Vector<Integer> aux = new Vector<Integer>();

		private int[][] mCostos = { { 0, 3, 0, 3, 0, 0, 3, 0, 0, 0, 0, 0, 0 },
				{ 4, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 3, 0, 0 },
				{ 4, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 3, 0 },
				{ 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 2, 0, 0 },
				{ 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
				{ 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1 },
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0 }, };

		/**
		 * Take as Heuristic the minimum cost (taking all lines) from
		 * the origin to destination
		 */
		private int[][] mHeuristica = {
				{ 0, 4, 4, 3, 4, 5, 3, 4, 4, 5, 7, 6, 5 },
				{ 4, 0, 11, 13, 4, 3, 5, 5, 17, 2, 7, 3, 3 },
				{ 4, 12, 0, 2, 5, 7, 6, 4, 1, 11, 3, 10, 10 },
				{ 4, 14, 1, 0, 6, 9, 7, 5, 1, 13, 4, 12, 12 },
				{ 5, 6, 4, 6, 0, 1, 1, 1, 5, 5, 3, 4, 4 },
				{ 7, 5, 7, 9, 1, 0, 2, 1, 8, 4, 3, 3, 3 },
				{ 4, 7, 4, 6, 1, 2, 0, 2, 5, 7, 3, 5, 6 },
				{ 5, 7, 3, 5, 1, 1, 2, 0, 4, 6, 2, 4, 5 },
				{ 5, 8, 1, 1, 6, 5, 7, 5, 0, 9, 4, 8, 9 },
				{ 5, 1, 10, 8, 9, 10, 8, 10, 9, 0, 11, 2, 1 },
				{ 7, 7, 1, 3, 2, 1, 3, 1, 2, 6, 0, 4, 5 },
				{ 7, 3, 12, 10, 11, 12, 10, 12, 11, 2, 13, 0, 1 },
				{ 6, 2, 11, 9, 10, 11, 9, 11, 10, 1, 13, 1, 0 } };

		

		@Override
		protected Vector<Integer> doInBackground(Void... index) {

			aux.addElement(Origen);
			
			/** Origin as first element*/
			mRuta.addElement(aux);

			/** While the best path don't contains the destination  */
			while (!(mRuta.lastElement().lastElement() == Desti)) {

				/** Take the last element (optimum) */ 
				C = mRuta.lastElement();
				/** Expand it */
				E = Expandir(C, Desti);
				/** Insert the new path in order (cost) */
				mRuta = InsercioOrdenada(E, mRuta);
			}

			return mRuta.lastElement();
		}

		@Override
		protected void onPostExecute(Vector<Integer> result) {
			try {
				dialogmaker();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}

		private Vector<Vector<Integer>> InsercioOrdenada(Vector<Integer> E,
				Vector<Vector<Integer>> mRuta) {

			int cost = 0;
			int costE = 0;
			int i = 0;

			boolean found = false;
			Vector<Integer> aux = new Vector<Integer>();

			/** Calculate the cost of E */
			for (int z = 0; z < E.size() - 1; z++) {

				costE += mCostos[z][z + 1];
			}
			costE += mHeuristica[E.lastElement()][Desti];

			/** Place E in the correct position taking account of next element (path array) */
			while (!found && i < mRuta.size()) {
				aux = mRuta.get(i);

				for (int j = 0; j < aux.size() - 1; j++) {
					cost += mCostos[j][j + 1];
				}
				cost += mHeuristica[aux.lastElement()][Desti];
				if (costE >= cost) {
					mRuta.add(i, E);
					found = true;
				}
				cost = 0;
				i++;
			}

			return mRuta;

		}

		private Vector<Integer> Expandir(Vector<Integer> C, int desti) {

			Vector<Integer> E = new Vector<Integer>();
			int cost, aux;
			E = C;
			/** Take as index the last element added to the path */
			int IndexaExpandir = C.lastElement();
			int i = 0;

			/** Use a big value to set on the first loop */
			aux = 999;

			do {

				if (mCostos[IndexaExpandir][i] != 0) {

					cost = mCostos[IndexaExpandir][i] + mHeuristica[i][Desti];

					if (aux > cost) {
						aux = cost;
					}
				}
				i++;
			} while (i < 13);

			for (int j = 0; j < 13; j++) {
				if ((mCostos[IndexaExpandir][j] != 0)
						&& (aux == mCostos[IndexaExpandir][j]
								+ mHeuristica[j][desti])) {
					/** Add the position of the next "node" on the path*/
					E.addElement(j);
					break;
				}
			}
			return E;
		}

	}

	/** */
	public void dialogmaker() throws InterruptedException, ExecutionException {

		ruta = AstarTask.get();
		Collections.addAll(mNoms, mNomsString);
		String ruta_text = "";

		for (int i = 0; i < ruta.size(); i++) {
			ruta_text += mNoms.get(ruta.get(i)) + " -> ";
		}

		DialogFragment df = new Dialog(ruta_text);
		df.show(getFragmentManager(), "Ruta");

	}

}
