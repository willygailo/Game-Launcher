package com.gamespace.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamespace.app.R;
import com.gamespace.app.core.GameAppInfo;
import com.gamespace.app.data.GameManagerRepository;

import java.util.List;

public class GamesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        Button btnBoost = view.findViewById(R.id.btn_boost_ram);
        TextView tvHeader = view.findViewById(R.id.tv_games_header);
        RecyclerView rvGames = view.findViewById(R.id.rv_games_list);

        rvGames.setLayoutManager(new LinearLayoutManager(getContext()));

        btnBoost.setOnClickListener(v -> {
            if (getContext() != null) {
                String resultMsg = GameManagerRepository.boostRamAndOptimize(getContext());
                Toast.makeText(getContext(), resultMsg, Toast.LENGTH_LONG).show();
            }
        });

        if (getContext() != null) {
            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(getContext());
            tvHeader.setText("INSTALLED GAMES DETECTED (" + installedGames.size() + "):");
            GamesAdapter adapter = new GamesAdapter(getContext(), installedGames);
            rvGames.setAdapter(adapter);
        }

        return view;
    }
}
