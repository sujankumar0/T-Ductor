package com.example.t_ductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.t_ductor.Model.Language;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText et_native_language;
    TextView tv_destiny_language;
    MaterialButton btn_select_language, btn_selected_language, btn_translate;
    private ArrayList<Language> languagesArrayList;

    private String code_native_language = "es";
    private String title_native_language = "Español";

    private String code_destiny_language = "en";

    private String title_destiny_language = "Inglés";

    private ProgressDialog progressDialog;

    private TranslatorOptions translatorOptions;
    private Translator translator;
    private String text_native_language = "";

    Dialog dialog;


    private static final String RECORDS = "Mis Registros";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        availableLanguages();

        btn_select_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Elegir Idioma", Toast.LENGTH_SHORT).show();
                selectNativeLanguage();
            }
        });

        btn_selected_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Idioma Elegido", Toast.LENGTH_SHORT).show();
                selectDestinyLanguage();
            }
        });

        btn_translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Traducir", Toast.LENGTH_SHORT).show();
                validateData();
            }
        });
    }


    private void initializeViews(){
        et_native_language = findViewById(R.id.et_native_language);
        tv_destiny_language = findViewById(R.id.tv_destiny_language);
        btn_select_language = findViewById(R.id.btn_select_language);
        btn_selected_language = findViewById(R.id.btn_selected_language);
        btn_translate = findViewById(R.id.btn_translate);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);
        dialog = new Dialog(MainActivity.this);
    }

    private void availableLanguages(){
        languagesArrayList = new ArrayList<>();
        List<String> languageCodeList = TranslateLanguage.getAllLanguages();

        for (String code_language : languageCodeList){
            String language_title = new Locale(code_language).getDisplayLanguage();

            //Log.d(RECORDS, "IdiomasDisponibles: code_language:" + code_language);
            //Log.d(RECORDS, "IdiomasDisponibles: language_title:" + language_title);

            Language model = new Language(code_language, language_title);
            languagesArrayList.add(model);
        }
    }

    private void selectNativeLanguage(){
        PopupMenu popupMenu = new PopupMenu(this, btn_select_language);
        for(int i = 0; i<languagesArrayList.size(); i++){
            popupMenu.getMenu().add(Menu.NONE, i, i, languagesArrayList.get(i).getLanguageTitle());

            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int position = item.getItemId();

                    code_native_language = languagesArrayList.get(position).getLanguageCode();
                    title_native_language = languagesArrayList.get(position).getLanguageTitle();

                    btn_select_language.setText(title_native_language);
                    et_native_language.setHint("Ingrese texto en: " + title_native_language);

                    Log.d(RECORDS, "onMenuItemClick: code_language:" + code_native_language);
                    Log.d(RECORDS, "onMenuItemClick: language_title:" + title_native_language);

                    return false;
                }
            });
        }
    }

    private void selectDestinyLanguage(){
        PopupMenu popupMenu = new PopupMenu(this, btn_selected_language);
        for(int i = 0; i<languagesArrayList.size(); i++) {
            popupMenu.getMenu().add(Menu.NONE, i, i, languagesArrayList.get(i).getLanguageTitle());

            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int position = item.getItemId();

                    code_destiny_language = languagesArrayList.get(position).getLanguageCode();
                    title_destiny_language = languagesArrayList.get(position).getLanguageTitle();

                    btn_selected_language.setText(title_destiny_language);

                    Log.d(RECORDS, "onMenuItemClick: code_destiny_language:" + code_destiny_language);
                    Log.d(RECORDS, "onMenuItemClick: title_destiny_language:" + title_destiny_language);

                    return false;
                }
            });
        }
    }

    private void validateData() {
        text_native_language = et_native_language.getText().toString().trim();
        Log.d(RECORDS, "ValidarDatos: "+ text_native_language);
        if (text_native_language.isEmpty()){
            Toast.makeText(this, "Ingrese texto", Toast.LENGTH_SHORT).show();
        }else{
            translateText();
        }
    }

    private void translateText() {
        progressDialog.setMessage("Procesando");
        progressDialog.show();

        translatorOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(code_native_language)
                .setTargetLanguage(code_destiny_language)
                .build();

        translator = Translation.getClient(translatorOptions);

        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Los paquetes de traduccion se descargaron con éxito
                        Log.d(RECORDS, "onSucces: El paquete se ha descargado");
                        progressDialog.setMessage("Traduciendo texto");

                        translator.translate(text_native_language)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String tranlasted_text) {
                                        progressDialog.dismiss();
                                        Log.d(RECORDS, "OnSucces: texto_traducido"+tranlasted_text);
                                        tv_destiny_language.setText(tranlasted_text);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.d(RECORDS, "OnFailure"+e);
                                        Toast.makeText(MainActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Los paquetes de traduccion no se descargaron con éxito
                        progressDialog.dismiss();
                        Log.d(RECORDS, "onFailure"+e);
                        Toast.makeText(MainActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDialog(){
        Button btn_okay;
        dialog.setContentView(R.layout.custom_dialog);

        btn_okay = dialog.findViewById(R.id.btn_okay);

        btn_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_clean_text){
            String string_text = "Traducción";
            et_native_language.setText("");
            et_native_language.setHint("Ingrese texto");
            tv_destiny_language.setText(string_text);
        }

        if (item.getItemId() == R.id.menu_info){
            showDialog();
        }

        return super.onOptionsItemSelected(item);
    }
}