package com.minimalist.calculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_MEDIA = 1001;
    private static final String[] BUTTONS = {
            "AC", "+/-", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "−",
            "1", "2", "3", "+",
            "0", ".", "="
    };
    private PasscodeManager passcodeManager;
    private TextView display;
    private final ArrayList<String> taps = new ArrayList<>();
    private final ArrayList<String> setupFirst = new ArrayList<>();
    private boolean setupConfirm;
    private String input = "0";
    private String displayText = "0";
    private double storedValue;
    private String pendingOperator;
    private boolean resetInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        passcodeManager = new PasscodeManager(this);
        showCalculator();
        if (!passcodeManager.hasPasscode()) {
            display.postDelayed(this::showSetupPrompt, 350);
        }
    }

    private void showCalculator() {
        taps.clear();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.BOTTOM);
        root.setPadding(dp(14), dp(26), dp(14), dp(14));
        root.setBackgroundColor(Color.BLACK);

        display = new TextView(this);
        display.setText(displayText);
        display.setTextColor(Color.WHITE);
        display.setTextSize(72);
        display.setGravity(Gravity.END | Gravity.BOTTOM);
        display.setSingleLine(true);
        display.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        root.addView(display, new LinearLayout.LayoutParams(-1, 0, 1));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(5);
        root.addView(grid, new LinearLayout.LayoutParams(-1, -2));

        for (String label : BUTTONS) {
            Button button = calculatorButton(label, 26);
            button.setOnClickListener(v -> onCalculatorTap(label));
            grid.addView(button, gridParams(label, 82));
        }

        setContentView(root);
    }

    private void onCalculatorTap(String label) {
        recordTap(label);
        calculate(label);
        display.setText(displayText);
    }

    private void recordTap(String label) {
        if (!passcodeManager.hasPasscode()) {
            return;
        }
        taps.add(label);
        int passLength = passcodeManager.length();
        if (taps.size() > passLength) {
            taps.remove(0);
        }
        if (taps.size() == passLength && passcodeManager.matches(taps)) {
            taps.clear();
            showVault();
        }
    }

    private void calculate(String label) {
        if ("AC".equals(label)) {
            input = "0";
            displayText = input;
            storedValue = 0;
            pendingOperator = null;
            resetInput = false;
        } else if ("+/-".equals(label)) {
            if (input.startsWith("-")) {
                input = input.substring(1);
            } else if (!"0".equals(input)) {
                input = "-" + input;
            }
            displayText = input;
        } else if ("%".equals(label)) {
            input = format(parseInput() / 100d);
            displayText = input;
        } else if (isOperator(label)) {
            applyPending();
            pendingOperator = label;
            resetInput = true;
            displayText = input + " " + label;
        } else if ("=".equals(label)) {
            applyPending();
            pendingOperator = null;
            resetInput = true;
            displayText = input;
        } else if (".".equals(label)) {
            if (resetInput) {
                input = "0";
                resetInput = false;
            }
            if (!input.contains(".")) {
                input += ".";
            }
            displayText = input;
        } else {
            if (resetInput || "0".equals(input)) {
                input = label;
                resetInput = false;
            } else if (input.length() < 10) {
                input += label;
            }
            displayText = input;
        }
    }

    private void applyPending() {
        double current = parseInput();
        if (pendingOperator == null) {
            storedValue = current;
            return;
        }
        switch (pendingOperator) {
            case "+":
                storedValue += current;
                break;
            case "−":
                storedValue -= current;
                break;
            case "×":
                storedValue *= current;
                break;
            case "÷":
                storedValue = current == 0 ? 0 : storedValue / current;
                break;
            default:
                storedValue = current;
                break;
        }
        input = format(storedValue);
    }

    private double parseInput() {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0000001) {
            return String.format(Locale.US, "%.0f", value);
        }
        String text = String.format(Locale.US, "%.8f", value);
        while (text.contains(".") && text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }

    private boolean isOperator(String label) {
        return "+".equals(label) || "−".equals(label) || "×".equals(label) || "÷".equals(label);
    }

    private int buttonColor(String label) {
        if (isOperator(label) || "=".equals(label)) {
            return Color.rgb(255, 159, 10);
        }
        if ("AC".equals(label) || "+/-".equals(label) || "%".equals(label)) {
            return Color.rgb(165, 165, 165);
        }
        return Color.rgb(51, 51, 51);
    }

    private int textColor(String label) {
        if ("AC".equals(label) || "+/-".equals(label) || "%".equals(label)) {
            return Color.BLACK;
        }
        return Color.WHITE;
    }

    private void showSetupPrompt() {
        new AlertDialog.Builder(this)
                .setTitle("Set hidden passcode")
                .setMessage("Tap a memorable calculator button sequence. Use at least 6 taps. You can only change it after unlocking the vault.")
                .setPositiveButton("Start", (dialog, which) -> startSetup(false))
                .setCancelable(false)
                .show();
    }

    private void startSetup(boolean fromVault) {
        setupFirst.clear();
        setupConfirm = false;
        taps.clear();
        input = "0";
        displayText = input;
        showSetupScreen(fromVault);
    }

    private void showSetupScreen(boolean fromVault) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(18), dp(30), dp(18), dp(18));
        root.setBackgroundColor(Color.BLACK);

        TextView title = text(setupConfirm ? "Confirm passcode" : "Create passcode", 28, Color.WHITE, Gravity.CENTER);
        TextView subtitle = text("Use calculator buttons only. Minimum " + PasscodeManager.MIN_TAPS + " taps.", 15, Color.rgb(170, 170, 170), Gravity.CENTER);
        display = text(mask(setupConfirm ? taps.size() : setupFirst.size()), 44, Color.WHITE, Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));
        root.addView(display, new LinearLayout.LayoutParams(-1, dp(90)));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        root.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        for (String label : BUTTONS) {
            Button button = calculatorButton(label, 22);
            button.setOnClickListener(v -> onSetupTap(label, fromVault));
            grid.addView(button, gridParams(label, 70));
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        actions.setPadding(0, dp(14), 0, 0);
        Button clear = actionButton("Clear");
        clear.setOnClickListener(v -> {
            if (setupConfirm) {
                taps.clear();
            } else {
                setupFirst.clear();
            }
            display.setText(mask(0));
        });
        Button next = actionButton(setupConfirm ? "Save" : "Next");
        next.setOnClickListener(v -> finishSetupStep(fromVault));
        actions.addView(clear, new LinearLayout.LayoutParams(0, dp(50), 1));
        actions.addView(next, new LinearLayout.LayoutParams(0, dp(50), 1));
        root.addView(actions, new LinearLayout.LayoutParams(-1, -2));

        if (fromVault) {
            Button cancel = actionButton("Back to Vault");
            cancel.setOnClickListener(v -> showVault());
            root.addView(cancel, new LinearLayout.LayoutParams(-1, dp(52)));
        }

        setContentView(root);
    }

    private void onSetupTap(String label, boolean fromVault) {
        if (setupConfirm) {
            taps.add(label);
            display.setText(mask(taps.size()));
        } else {
            setupFirst.add(label);
            display.setText(mask(setupFirst.size()));
        }
    }

    private void finishSetupStep(boolean fromVault) {
        if (!setupConfirm) {
            if (setupFirst.size() < PasscodeManager.MIN_TAPS) {
                toast("Use at least " + PasscodeManager.MIN_TAPS + " taps");
                return;
            }
            setupConfirm = true;
            taps.clear();
            showSetupScreen(fromVault);
            return;
        }
        if (!PasscodeManager.same(setupFirst, taps)) {
            toast("Sequences did not match");
            setupFirst.clear();
            taps.clear();
            setupConfirm = false;
            showSetupScreen(fromVault);
            return;
        }
        try {
            passcodeManager.save(setupFirst);
            toast("Passcode saved");
            setupFirst.clear();
            taps.clear();
            if (fromVault) {
                showVault();
            } else {
                showCalculator();
            }
        } catch (Exception e) {
            toast("Could not save passcode");
        }
    }

    private String mask(int count) {
        if (count == 0) {
            return "";
        }
        char[] chars = new char[count];
        Arrays.fill(chars, '•');
        return new String(chars);
    }

    private void showVault() {
        cleanupCache();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(14), dp(28), dp(14), dp(14));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("Vault", 30, Color.WHITE, Gravity.START);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button add = actionButton("Add");
        add.setOnClickListener(v -> pickMedia());
        Button pass = actionButton("Passcode");
        pass.setOnClickListener(v -> startSetup(true));
        top.addView(add, new LinearLayout.LayoutParams(dp(92), dp(48)));
        top.addView(pass, new LinearLayout.LayoutParams(dp(112), dp(48)));
        root.addView(top, new LinearLayout.LayoutParams(-1, -2));

        TextView note = text("Files are encrypted and stored in app-private storage.", 14, Color.rgb(155, 155, 155), Gravity.START);
        root.addView(note, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dp(14), 0, dp(14));
        scroll.addView(list);
        File[] files = vaultDir().listFiles((dir, name) -> name.endsWith(".vault"));
        if (files == null || files.length == 0) {
            TextView empty = text("No photos or videos yet", 20, Color.rgb(190, 190, 190), Gravity.CENTER);
            list.addView(empty, new LinearLayout.LayoutParams(-1, dp(220)));
        } else {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            for (File file : files) {
                list.addView(mediaRow(file));
            }
        }
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        Button lock = actionButton("Lock");
        lock.setOnClickListener(v -> showCalculator());
        root.addView(lock, new LinearLayout.LayoutParams(-1, dp(54)));
        setContentView(root);
    }

    private View mediaRow(File file) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(8), dp(10));
        TextView name = text(displayName(file), 16, Color.WHITE, Gravity.START);
        TextView meta = text(file.length() / 1024 + " KB", 13, Color.rgb(150, 150, 150), Gravity.START);
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(name);
        labels.addView(meta);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        Button open = actionButton("Open");
        open.setOnClickListener(v -> openVaultFile(file));
        Button delete = actionButton("Delete");
        delete.setOnClickListener(v -> confirmDelete(file));
        row.addView(open, new LinearLayout.LayoutParams(dp(88), dp(46)));
        row.addView(delete, new LinearLayout.LayoutParams(dp(98), dp(46)));
        return row;
    }

    private void pickMedia() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        try {
            startActivityForResult(intent, PICK_MEDIA);
        } catch (ActivityNotFoundException e) {
            toast("No file picker found");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MEDIA && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importMedia(data.getData());
        }
    }

    private void importMedia(Uri uri) {
        toast("Importing...");
        new Thread(() -> {
            String name = sanitize(queryName(uri));
            File output = new File(vaultDir(), System.currentTimeMillis() + "_" + name + ".vault");
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream == null) {
                    runOnUiThread(() -> toast("Could not read file"));
                    return;
                }
                Crypto.encryptStream(this, inputStream, output);
                runOnUiThread(() -> {
                    toast("Added to vault");
                    showVault();
                });
            } catch (Exception e) {
                output.delete();
                runOnUiThread(() -> toast("Import failed"));
            }
        }).start();
    }

    private void openVaultFile(File file) {
        showOpeningScreen(file);
        new Thread(() -> {
            try {
                File cacheFile = new File(getCacheDir(), "preview_" + displayName(file));
                Crypto.decryptFile(this, file, cacheFile);
                runOnUiThread(() -> showPreview(cacheFile));
            } catch (Exception e) {
                runOnUiThread(() -> toast("Could not open file"));
            }
        }).start();
    }

    private void showOpeningScreen(File file) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(28), dp(24), dp(24));
        root.setBackgroundColor(Color.BLACK);
        root.addView(text("Preparing preview...", 24, Color.WHITE, Gravity.CENTER), new LinearLayout.LayoutParams(-1, -2));
        root.addView(text(displayName(file), 15, Color.rgb(170, 170, 170), Gravity.CENTER), new LinearLayout.LayoutParams(-1, -2));
        Button back = actionButton("Back");
        back.setOnClickListener(v -> showVault());
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, dp(54));
        backParams.setMargins(0, dp(24), 0, 0);
        root.addView(back, backParams);
        setContentView(root);
    }

    private void showPreview(File file) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(12), dp(28), dp(12), dp(12));
        TextView title = text(file.getName(), 18, Color.WHITE, Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        String lower = file.getName().toLowerCase(Locale.US);
        if (lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mkv") || lower.endsWith(".3gp")) {
            VideoView videoView = new VideoView(this);
            videoView.setVideoURI(Uri.fromFile(file));
            videoView.setOnPreparedListener(mp -> videoView.start());
            root.addView(videoView, new LinearLayout.LayoutParams(-1, 0, 1));
            Button pause = actionButton("Pause");
            pause.setOnClickListener(v -> {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    pause.setText("Play");
                } else {
                    videoView.start();
                    pause.setText("Pause");
                }
            });
            root.addView(pause, new LinearLayout.LayoutParams(-1, dp(54)));
        } else {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageURI(Uri.fromFile(file));
            root.addView(imageView, new LinearLayout.LayoutParams(-1, 0, 1));
        }
        Button back = actionButton("Back");
        back.setOnClickListener(v -> showVault());
        root.addView(back, new LinearLayout.LayoutParams(-1, dp(54)));
        setContentView(root);
    }

    private void confirmDelete(File file) {
        new AlertDialog.Builder(this)
                .setTitle("Delete file?")
                .setMessage("This permanently removes the encrypted vault copy.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (file.delete()) {
                        toast("Deleted");
                    }
                    showVault();
                })
                .show();
    }

    private File vaultDir() {
        File dir = new File(getFilesDir(), "vault_media");
        dir.mkdirs();
        return dir;
    }

    private void cleanupCache() {
        File[] files = getCacheDir().listFiles((dir, name) -> name.startsWith("preview_"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }

    private String queryName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
        }
        return "media";
    }

    private String sanitize(String name) {
        String cleaned = name == null ? "media" : name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (cleaned.length() > 80) {
            cleaned = cleaned.substring(cleaned.length() - 80);
        }
        return cleaned.isEmpty() ? "media" : cleaned;
    }

    private String displayName(File file) {
        String name = file.getName();
        int underscore = name.indexOf('_');
        if (underscore >= 0 && underscore + 1 < name.length()) {
            name = name.substring(underscore + 1);
        }
        if (name.endsWith(".vault")) {
            name = name.substring(0, name.length() - 6);
        }
        return name;
    }

    private Button calculatorButton(String label, int textSize) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(textSize);
        button.setTextColor(textColor(label));
        button.setBackground(rounded(buttonColor(label), 40));
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private GridLayout.LayoutParams gridParams(String label, int heightDp) {
        int cell = buttonWidth();
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = label.equals("0") ? cell * 2 + dp(8) : cell;
        params.height = dp(heightDp);
        params.setMargins(dp(4), dp(5), dp(4), dp(5));
        if (label.equals("0")) {
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 2);
        }
        return params;
    }

    private int buttonWidth() {
        int screen = getResources().getDisplayMetrics().widthPixels;
        return Math.max(dp(62), (screen - dp(60)) / 4);
    }

    private Button actionButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setBackground(rounded(Color.rgb(45, 45, 45), 24));
        button.setMinHeight(0);
        button.setMinWidth(0);
        return button;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private TextView text(String value, int size, int color, int gravity) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(size);
        textView.setTextColor(color);
        textView.setGravity(gravity);
        textView.setPadding(dp(4), dp(5), dp(4), dp(5));
        return textView;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cleanupCache();
    }
}
