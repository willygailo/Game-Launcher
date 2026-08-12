package com.gamebooster.app.feature.sensitivity.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.gamebooster.app.feature.sensitivity.model.SensitivityCalculator;
import com.gamebooster.app.feature.sensitivity.model.SensitivityModel;

/**
 * SensitivityFragment provides an interactive cross-game sensitivity calculator UI.
 * Computes eDPI-based sensitivity recommendations for PUBG Mobile, CODM, Free Fire, and MLBB.
 */
public class SensitivityFragment extends Fragment {

    private EditText etDpi;
    private EditText etScreenSize;
    private EditText etGyroMult;
    private TextView tvResults;
    private String lastResultText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = requireContext();

        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        scrollView.setBackgroundColor(Color.parseColor("#0F0C20"));
        scrollView.setPadding(32, 32, 32, 32);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Title
        TextView tvTitle = new TextView(context);
        tvTitle.setText("🎯 CROSS-GAME SENSITIVITY CALCULATOR");
        tvTitle.setTextSize(18f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#00F0FF"));
        tvTitle.setPadding(0, 0, 0, 16);
        root.addView(tvTitle);

        // Subtitle
        TextView tvSub = new TextView(context);
        tvSub.setText("Calculate optimal scope & gyro sensitivity based on your device DPI and screen size.");
        tvSub.setTextSize(13f);
        tvSub.setTextColor(Color.parseColor("#8F92A1"));
        tvSub.setPadding(0, 0, 0, 32);
        root.addView(tvSub);

        // Input 1: DPI
        root.addView(createLabel(context, "Touch DPI (e.g. 400 - 1200):"));
        etDpi = createEditText(context, "600", InputType.TYPE_CLASS_NUMBER);
        root.addView(etDpi);

        // Input 2: Screen Size
        root.addView(createLabel(context, "Screen Diagonal Size (Inches, e.g. 6.57):"));
        etScreenSize = createEditText(context, "6.5", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        root.addView(etScreenSize);

        // Input 3: Gyro Multiplier
        root.addView(createLabel(context, "Gyroscope Multiplier (e.g. 1.0 - 2.0):"));
        etGyroMult = createEditText(context, "1.5", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        root.addView(etGyroMult);

        // Calculate Button
        Button btnCalc = new Button(context);
        btnCalc.setText("⚡ CALCULATE RECOMMENDED SENSITIVITY");
        btnCalc.setBackgroundColor(Color.parseColor("#0072FF"));
        btnCalc.setTextColor(Color.WHITE);
        btnCalc.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 24, 0, 16);
        btnCalc.setLayoutParams(btnParams);
        btnCalc.setOnClickListener(v -> calculateSens());
        root.addView(btnCalc);

        // Copy Button
        Button btnCopy = new Button(context);
        btnCopy.setText("📋 COPY TO CLIPBOARD");
        btnCopy.setBackgroundColor(Color.parseColor("#1C1C2E"));
        btnCopy.setTextColor(Color.parseColor("#00FF66"));
        btnCopy.setTypeface(null, Typeface.BOLD);
        btnCopy.setLayoutParams(btnParams);
        btnCopy.setOnClickListener(v -> {
            if (lastResultText.isEmpty()) {
                Toast.makeText(context, "Calculate sensitivity first!", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("eSports Sensitivity", lastResultText));
                Toast.makeText(context, "📋 Sensitivity copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(btnCopy);

        // Results Box
        tvResults = new TextView(context);
        tvResults.setText("Tap 'CALCULATE' to view recommended values.");
        tvResults.setTextSize(13f);
        tvResults.setTextColor(Color.parseColor("#E0E0E0"));
        tvResults.setBackgroundColor(Color.parseColor("#16122E"));
        tvResults.setPadding(24, 24, 24, 24);
        LinearLayout.LayoutParams resParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        resParams.setMargins(0, 16, 0, 32);
        tvResults.setLayoutParams(resParams);
        root.addView(tvResults);

        scrollView.addView(root);
        return scrollView;
    }

    private TextView createLabel(Context context, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setTextColor(Color.parseColor("#A0A5B5"));
        tv.setPadding(0, 12, 0, 4);
        return tv;
    }

    private EditText createEditText(Context context, String defaultValue, int inputType) {
        EditText et = new EditText(context);
        et.setText(defaultValue);
        et.setInputType(inputType);
        et.setTextSize(14f);
        et.setTextColor(Color.WHITE);
        et.setHintTextColor(Color.GRAY);
        et.setBackgroundColor(Color.parseColor("#1C1C2E"));
        et.setPadding(20, 16, 20, 16);
        return et;
    }

    private void calculateSens() {
        try {
            int dpi = Integer.parseInt(etDpi.getText().toString().trim());
            double screen = Double.parseDouble(etScreenSize.getText().toString().trim());
            float gyro = Float.parseFloat(etGyroMult.getText().toString().trim());

            SensitivityModel m = SensitivityCalculator.calculate(dpi, screen, gyro);

            StringBuilder sb = new StringBuilder();
            sb.append("📊 RECOMMENDED eSPORTS SENSITIVITY (DPI ").append(dpi).append("):\n\n");
            sb.append("🎯 CAMERA / ADS SENSITIVITY:\n");
            sb.append("  • Free Look: ").append(m.freeLook).append("\n");
            sb.append("  • 3rd Person No Scope: ").append(m.noScope3rdPerson).append("\n");
            sb.append("  • Red Dot / Holo: ").append(m.redDotHolo).append("\n");
            sb.append("  • 2x Scope: ").append(m.scope2x).append("\n");
            sb.append("  • 3x Scope: ").append(m.scope3x).append("\n");
            sb.append("  • 4x Scope: ").append(m.scope4x).append("\n");
            sb.append("  • 6x Scope: ").append(m.scope6x).append("\n");
            sb.append("  • 8x Scope: ").append(m.scope8x).append("\n\n");

            sb.append("🌀 GYROSCOPE SENSITIVITY:\n");
            sb.append("  • Gyro No Scope: ").append(m.gyroNoScope).append("\n");
            sb.append("  • Gyro Red Dot: ").append(m.gyroRedDot).append("\n");
            sb.append("  • Gyro 2x: ").append(m.gyro2x).append("\n");
            sb.append("  • Gyro 3x: ").append(m.gyro3x).append("\n");
            sb.append("  • Gyro 4x: ").append(m.gyro4x).append("\n");
            sb.append("  • Gyro 6x: ").append(m.gyro6x).append("\n");
            sb.append("  • Gyro 8x: ").append(m.gyro8x).append("\n\n");

            sb.append("🎮 GAME CROSS-CONVERSION EQUIVALENTS:\n");
            sb.append("  • PUBG Mobile: ").append(m.redDotHolo).append(" ADS / ").append(m.gyroRedDot).append(" Gyro\n");
            sb.append("  • COD Mobile: ").append(Math.round(m.redDotHolo * 1.25)).append(" Standard / ").append(Math.round(m.gyroRedDot * 1.1)).append(" Gyro\n");
            sb.append("  • Free Fire: ").append(Math.min(100, Math.round(m.redDotHolo * 1.6))).append(" Red Dot\n");
            sb.append("  • MLBB Skill Camera: ").append(Math.min(100, Math.round(m.freeLook * 1.1))).append("% Speed");

            lastResultText = sb.toString();
            tvResults.setText(lastResultText);
            Toast.makeText(requireContext(), "⚡ Sensitivity Calculated!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Please enter valid numerical values!", Toast.LENGTH_SHORT).show();
        }
    }

    public static SensitivityFragment newInstance() {
        return new SensitivityFragment();
    }
}
