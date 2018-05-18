package com.qi0.weslley.gerenciadordediscursos.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.qi0.weslley.gerenciadordediscursos.Config.ConfiguracaoFirebase;
import com.qi0.weslley.gerenciadordediscursos.R;
import com.qi0.weslley.gerenciadordediscursos.activitys.AdicionarEditarActivity;
import com.qi0.weslley.gerenciadordediscursos.adapter.DiscursoAdapter;
import com.qi0.weslley.gerenciadordediscursos.helper.RecyclerItemClickListener;
import com.qi0.weslley.gerenciadordediscursos.model.Congregacao;
import com.qi0.weslley.gerenciadordediscursos.model.Discurso;

import java.util.ArrayList;
import java.util.Collections;

import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiscursosFragment extends BaseFragment {

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    DiscursoAdapter adapter;
    Discurso discursoSelecionado;

    ArrayList<Discurso> discursosList = new ArrayList();

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    ValueEventListener valueEventListenerDiscursos;
    String userUID;

    public DiscursosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_discursos, container, false);

        databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();
        firebaseAuth = ConfiguracaoFirebase.getAuth();

        userUID = firebaseAuth.getCurrentUser().getUid();

        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycle_view_discursos);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        adapter = new DiscursoAdapter(discursosList, getContext());

        recyclerView.setAdapter(adapter);

        runLayoutAnimation(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                FloatingActionButton fab = getActivity().findViewById(R.id.fab);

                if (dy > 0){
                    if (fab.isShown()){
                        fab.hide();
                    }
                }else if (dy < 0){
                    if (!fab.isShown()){
                        fab.show();
                    }
                }
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onLongItemClick(View view, int position) {
                discursoSelecionado = (Discurso) discursosList.get(position);
                showPopup(view);
            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        }));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        pegarDiscursosDoBanco();
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListenerDiscursos);
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void pegarDiscursosDoBanco() {

        valueEventListenerDiscursos = databaseReference.child("user_data").child(userUID).child("discursos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                discursosList.clear();
                for (DataSnapshot dados : dataSnapshot.getChildren()){
                    Discurso discurso = dados.getValue(Discurso.class);
                    discursosList.add(discurso);
                    Collections.sort(discursosList);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("RestrictedApi")
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_recycle_view, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_editar:
                        Intent intentEdtarDiscurso = new Intent(getActivity(), AdicionarEditarActivity.class);
                        intentEdtarDiscurso.putExtra("qualFragmentAbrir", "AddDiscursosFragment");
                        intentEdtarDiscurso.putExtra("discursoSelecionado", discursoSelecionado);
                        startActivity(intentEdtarDiscurso);
                        return true;
                    case R.id.item_deletar:
                        databaseReference.child("user_data").child(userUID).child("discursos").child(discursoSelecionado.getIdDiscurso()).removeValue();
                        Toasty.success(getContext(), "Discurso Deletado", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }
            }
        });
        @SuppressLint("RestrictedApi") MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popup.getMenu(), v);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END);
        menuHelper.show();
    }

}
