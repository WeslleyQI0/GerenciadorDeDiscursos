package com.qi0.weslley.gerenciadordediscursos.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
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
import com.qi0.weslley.gerenciadordediscursos.config.ConfiguracaoFirebase;
import com.qi0.weslley.gerenciadordediscursos.R;
import com.qi0.weslley.gerenciadordediscursos.activitys.AdicionarEditarActivity;
import com.qi0.weslley.gerenciadordediscursos.adapter.CongregacaoAdapter;
import com.qi0.weslley.gerenciadordediscursos.helper.RecyclerItemClickListener;
import com.qi0.weslley.gerenciadordediscursos.model.Congregacao;
import com.qi0.weslley.gerenciadordediscursos.model.Orador;

import java.util.ArrayList;
import java.util.Collections;

import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 */
public class CongregacoesFragment extends BaseFragment{

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    CongregacaoAdapter adapter;
    Congregacao congregacaoSelecionada;
    ArrayList congregacoesList = new ArrayList();
    ArrayList oradoresList = new ArrayList();

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    ValueEventListener valueEventListenerCongregacao;
    String userUID;


    public CongregacoesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_congregacoes, container, false);

        databaseReference = ConfiguracaoFirebase.getFirebaseDatabase();
        firebaseAuth = ConfiguracaoFirebase.getAuth();

        userUID = firebaseAuth.getCurrentUser().getUid();

        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycle_view_congregaçoes);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        adapter = new CongregacaoAdapter(congregacoesList,oradoresList, getContext());

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
                congregacaoSelecionada = (Congregacao) congregacoesList.get(position);
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
        pegarCongregacoesDoBanco();
        pegarQuantidadeOradores();
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListenerCongregacao);
    }

    private void runLayoutAnimation(final RecyclerView recyclerView) {
        final Context context = recyclerView.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void pegarCongregacoesDoBanco() {

        valueEventListenerCongregacao = databaseReference.child("user_data").child(userUID).child("congregacoes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                congregacoesList.clear();
                for (DataSnapshot dados : dataSnapshot.getChildren()){
                    Congregacao congregacao = dados.getValue(Congregacao.class);
                    congregacoesList.add(congregacao);
                    Collections.sort(congregacoesList);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Pega Todos os oradores e seta no adapter para poder pegar a quantidade
    private void pegarQuantidadeOradores(){

        valueEventListenerCongregacao = databaseReference.child("user_data").child(userUID).child("oradores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                oradoresList.clear();
                for (DataSnapshot dados : dataSnapshot.getChildren()){
                    Orador orador = dados.getValue(Orador.class);
                    oradoresList.add(orador);
                    Collections.sort(oradoresList);
                }

                adapter.notifyDataSetChanged();
                //getSupportActionBar().setTitle(String.valueOf(oradoresList.size()));
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
                        Intent intentEditarCongregacao = new Intent(getActivity(), AdicionarEditarActivity.class);
                        intentEditarCongregacao.putExtra("qualFragmentAbrir", "AddCongregacaoFragment");
                        intentEditarCongregacao.putExtra("congregacaoelecionada", congregacaoSelecionada);
                        startActivity(intentEditarCongregacao);
                        return true;
                    case R.id.item_deletar:
                        databaseReference.child("user_data").child(userUID).child("congregacoes").child(congregacaoSelecionada.getIdCongregacao()).removeValue();
                        Toasty.success(getContext(), "Congregação Deletada", Toast.LENGTH_SHORT).show();
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
