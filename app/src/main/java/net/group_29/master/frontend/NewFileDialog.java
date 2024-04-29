package net.group_29.master.frontend;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;

import net.group_29.master.ApplicationObject;
import net.group_29.master.R;
import net.group_29.master.format.todotxt.TodoTxtTask;
import net.group_29.master.frontend.textview.HighlightingEditor;
import net.group_29.master.frontend.textview.TextViewUtils;
import net.group_29.master.model.AppSettings;
import net.group_29.master.model.Document;
import net.group_29.master.util.MarkorContextUtils;
import net.group_29.opoc.util.GsContextUtils;
import net.group_29.opoc.util.GsFileUtils;
import net.group_29.opoc.wrapper.GsAndroidSpinnerOnItemSelectedAdapter;
import net.group_29.opoc.wrapper.GsCallback;

import java.io.File;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import other.de.stanetz.jpencconverter.JavaPasswordbasedCryption;

public class NewFileDialog extends DialogFragment {
    public static final String FRAGMENT_TAG = NewFileDialog.class.getName();
    public static final String EXTRA_DIR = "EXTRA_DIR";
    public static final String EXTRA_ALLOW_CREATE_DIR = "EXTRA_ALLOW_CREATE_DIR";
    private GsCallback.a2<Boolean, File> callback;

    public static NewFileDialog newInstance(final File sourceFile, final boolean allowCreateDir, final GsCallback.a2<Boolean, File> callback) {
        NewFileDialog dialog = new NewFileDialog();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_DIR, sourceFile);
        args.putSerializable(EXTRA_ALLOW_CREATE_DIR, allowCreateDir);
        dialog.setArguments(args);
        dialog.callback = callback;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final File file = (File) getArguments().getSerializable(EXTRA_DIR);
        final boolean allowCreateDir = getArguments().getBoolean(EXTRA_ALLOW_CREATE_DIR);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        AlertDialog.Builder dialogBuilder = makeDialog(file, allowCreateDir, inflater);
        AlertDialog dialog = dialogBuilder.show();
        Window w;
        if ((w = dialog.getWindow()) != null) {
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            // 设置圆角背景
            w.setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        }
        return dialog;
    }

    @SuppressLint("SetTextI18n")
    private AlertDialog.Builder makeDialog(final File basedir, final boolean allowCreateDir, LayoutInflater inflater) {
        View root;
        AlertDialog.Builder dialogBuilder;
        final AppSettings appSettings = ApplicationObject.settings();
        dialogBuilder = new AlertDialog.Builder(inflater.getContext(), R.style.Theme_AppCompat_DayNight_Dialog);
        root = inflater.inflate(R.layout.new_file_dialog, null);

        final EditText fileNameEdit = root.findViewById(R.id.new_file_dialog__name);
        final EditText fileExtEdit = root.findViewById(R.id.new_file_dialog__ext);
        final CheckBox encryptCheckbox = root.findViewById(R.id.new_file_dialog__encrypt);
        final CheckBox utf8BomCheckbox = root.findViewById(R.id.new_file_dialog__utf8_bom);
        final Spinner typeSpinner = root.findViewById(R.id.new_file_dialog__type);
        final Spinner templateSpinner = root.findViewById(R.id.new_file_dialog__template);
        final String[] typeSpinnerToExtension = getResources().getStringArray(R.array.new_file_types__file_extension);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && appSettings.isDefaultPasswordSet()) {
            encryptCheckbox.setChecked(appSettings.getNewFileDialogLastUsedEncryption());
        } else {
            encryptCheckbox.setVisibility(View.GONE);
        }
        utf8BomCheckbox.setChecked(appSettings.getNewFileDialogLastUsedUtf8Bom());
        utf8BomCheckbox.setVisibility(appSettings.isExperimentalFeaturesEnabled() ? View.VISIBLE : View.GONE);
        fileExtEdit.setText(appSettings.getNewFileDialogLastUsedExtension());
        fileNameEdit.requestFocus();
        new Handler().postDelayed(new GsContextUtils.DoTouchView(fileNameEdit), 200);

        fileNameEdit.setFilters(new InputFilter[]{GsContextUtils.instance.makeFilenameInputFilter()});
        fileExtEdit.setFilters(fileNameEdit.getFilters());

        loadTemplatesIntoSpinner(appSettings, templateSpinner);
        final AtomicBoolean typeSpinnerNoTriggerOnFirst = new AtomicBoolean(true);
        typeSpinner.setOnItemSelectedListener(new GsAndroidSpinnerOnItemSelectedAdapter(pos -> {
            if (pos == 3) { // Wikitext
                templateSpinner.setSelection(7); // Zim empty
            }
            if (typeSpinnerNoTriggerOnFirst.getAndSet(false)) {
                return;
            }
            String ext = pos < typeSpinnerToExtension.length ? typeSpinnerToExtension[pos] : "";

            if (ext != null) {
                if (encryptCheckbox.isChecked()) {
                    fileExtEdit.setText(ext + JavaPasswordbasedCryption.DEFAULT_ENCRYPTION_EXTENSION);
                } else {
                    fileExtEdit.setText(ext);
                }
            }
            fileNameEdit.setSelection(fileNameEdit.length());
            appSettings.setNewFileDialogLastUsedType(typeSpinner.getSelectedItemPosition());
        }));
        typeSpinner.setSelection(appSettings.getNewFileDialogLastUsedType());

        templateSpinner.setOnItemSelectedListener(new GsAndroidSpinnerOnItemSelectedAdapter(pos -> {
            String prefix = null;

            if (pos == 3) { // Jekyll
                prefix = TodoTxtTask.DATEF_YYYY_MM_DD.format(new Date()) + "-";
            } else if (pos == 9) { //ZettelKasten
                prefix = new SimpleDateFormat("yyyyMMddHHmm", Locale.ROOT).format(new Date()) + "-";
            }
            if (!TextUtils.isEmpty(prefix) && !fileNameEdit.getText().toString().startsWith(prefix)) {
                fileNameEdit.setText(prefix + fileNameEdit.getText().toString());
            }
            fileNameEdit.setSelection(fileNameEdit.length());
        }));

        encryptCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final String currentExtention = fileExtEdit.getText().toString();
            if (isChecked) {
                if (!currentExtention.endsWith(JavaPasswordbasedCryption.DEFAULT_ENCRYPTION_EXTENSION)) {
                    fileExtEdit.setText(currentExtention + JavaPasswordbasedCryption.DEFAULT_ENCRYPTION_EXTENSION);
                }
            } else if (currentExtention.endsWith(JavaPasswordbasedCryption.DEFAULT_ENCRYPTION_EXTENSION)) {
                fileExtEdit.setText(currentExtention.replace(JavaPasswordbasedCryption.DEFAULT_ENCRYPTION_EXTENSION, ""));
            }
            appSettings.setNewFileDialogLastUsedEncryption(isChecked);
        });

        utf8BomCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setNewFileDialogLastUsedUtf8Bom(isChecked);
        });

        dialogBuilder.setView(root);
        fileNameEdit.requestFocus();

        final MarkorContextUtils cu = new MarkorContextUtils(getContext());
        dialogBuilder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        dialogBuilder.setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> {
            if (ez(fileNameEdit)) {
                return;
            }

            appSettings.setNewFileDialogLastUsedExtension(fileExtEdit.getText().toString().trim());
            final String usedFilename = getFileNameWithoutExtension(fileNameEdit.getText().toString(), templateSpinner.getSelectedItemPosition());
            final File f = new File(basedir, Document.normalizeFilename(usedFilename.trim()) + fileExtEdit.getText().toString().trim());
            final Pair<byte[], Integer> templateContents = getTemplateContent(templateSpinner, basedir, f.getName(), encryptCheckbox.isChecked());
            cu.writeFile(getActivity(), f, false, (arg_ok, arg_fos) -> {
                try {
                    if (appSettings.getNewFileDialogLastUsedUtf8Bom()) {
                        arg_fos.write(0xEF);
                        arg_fos.write(0xBB);
                        arg_fos.write(0xBF);
                    }
                    if (templateContents.first != null && (!f.exists() || f.length() < GsContextUtils.TEXTFILE_OVERWRITE_MIN_TEXT_LENGTH)) {
                        arg_fos.write(templateContents.first);
                    }
                } catch (Exception ignored) {
                }
                if (templateContents.second >= 0) {
                    appSettings.setLastEditPosition(f.getAbsolutePath(), templateContents.second);
                }
                callback(arg_ok || f.exists(), f);
                dialogInterface.dismiss();
            });
        });

        dialogBuilder.setNeutralButton(R.string.folder, (dialogInterface, i) -> {
            if (ez(fileNameEdit)) {
                return;
            }
            final String usedFoldername = getFileNameWithoutExtension(fileNameEdit.getText().toString().trim(), templateSpinner.getSelectedItemPosition());
            final File f = new File(basedir, usedFoldername);
            if (cu.isUnderStorageAccessFolder(getContext(), f, true)) {
                DocumentFile dof = cu.getDocumentFile(getContext(), f, true);
                callback(dof != null && dof.exists(), f);
            } else {
                callback(f.mkdirs() || f.exists(), f);
            }
            dialogInterface.dismiss();
        });

        if (!allowCreateDir) {
            dialogBuilder.setNeutralButton("", null);
        }

        return dialogBuilder;
    }

    private void loadTemplatesIntoSpinner(final AppSettings appSettings, final Spinner templateSpinner) {
        List<String> templates = new ArrayList<>();
        for (int i = 0; i < templateSpinner.getCount(); i++) {
            templates.add((String) templateSpinner.getAdapter().getItem(i));
        }
        templates.addAll(MarkorDialogFactory.getSnippets(appSettings).keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, templates.toArray(new String[0]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        templateSpinner.setAdapter(adapter);
    }

    private boolean ez(EditText et) {
        return et.getText().toString().trim().isEmpty();
    }

    private String getFileNameWithoutExtension(String typedFilename, int selectedTemplatePos) {
        if (selectedTemplatePos == 7) {
            // Wikitext files always use underscores instead of spaces
            return typedFilename.trim().replace(' ', '_');
        }
        return typedFilename.trim();
    }

    private void callback(boolean ok, File file) {
        try {
            callback.callback(ok, file);
        } catch (Exception ignored) {
        }
    }

    // this code corresponds to R.arrays.arr_file_templates
    //
    // How to get content out of a file:
    // 1) Replace \n with \n | copy to clipboard
    //    cat master-markdown-reference.md  | sed 's@\\@\\\\@g' | sed -z 's@\n@\n@g'  | xclip
    //
    // 2) t = "<cursor>";  | ctrl+shift+v "paste without formatting"
    //
    // -----
    // if you use Androidstudio/IntelliJ copy file content into t = "<cursor>". Android studio takes care of escaping
    private String getTemplateByPos(
            final int spinnerPos,
            final String fileName,
            final File basedir
    ) {
        switch (spinnerPos) {
            case 2: {
                return "(A) Call Mom @mobile +family\n(A) Schedule annual checkup +health\n(A) Urgently buy milk @shop\n(B) Outline chapter 5 +novel @computer\n(C) Add cover sheets @work +myproject\nPlan backyard herb garden @home\nBuy salad @shop\nWrite blog post @pc\nInstall Markor @mobile\n2019-06-24 scan photos @home +blog\n2019-06-25 draw diagram @work \nx This has been done @home +renovations";
            }
            case 3: {
                return "---\nlayout: post\ntags: []\ncategories: []\n#date: 2019-06-25 13:14:15\n#excerpt: ''\n#image: 'BASEURL/assets/blog/img/.png'\n#description:\n#permalink:\ntitle: 'title'\n---\n\n\n";
            }
            case 4: {
                return "# Title\n## Description\n\n![Text](picture.png)\n\n### Ingredients\n\n|  Ingredient   | Amount |\n|:--------------|:-------|\n| 1             | 1      |\n| 2             | 2      |\n| 3             | 3      |\n| 4             | 4      |\n\n\n### Preparation\n\n1. Text\n2. Text\n\n";
            }
            case 5: {
                return "---\nclass: beamer\n---\n\n-----------------\n# Cool presentation\n\n## Abed Nadir\n\n{{ post.date_today }}\n\n<!-- Overall slide design -->\n<style>\n.slide {\nbackground:url() no-repeat center center fixed; background-size: cover;\n}\n.slide_type_title {\nbackground: slategrey;\n}\n</style>\n\n-----------------\n\n## Slide title\n\n\n1. All Markdown features of Markor are **supported** for Slides too ~~strikeout~~ _italic_ `code`\n2. Start new slides with 3 more hyphens (---) separated by empty lines\n3. End last slide with hyphens too\n4. Slide backgrounds can be configured using CSS, for all and individual slides\n5. Print / PDF export in landscape mode\n6. Create title only slides (like first slide) by starting the slide (line after `---`) with title `# title`\n\n\n-----------------\n## Slide with centered image\n* Images can be centered by adding 'imghcenter' in alt text & grown to page size with 'imgbig'\n* Example: `![text imghcenter imgbig text](a.jpg)`\n\n![imghcenter imgbig](file:///android_asset/img/flowerfield.jpg)\n\n\n\n\n-----------------\n## Page with gradient background\n* and a picture\n* configure background color/image with CSS .slide_p4 { } (4 = the slide page number)\n\n![pic](file:///android_asset/img/flowerfield.jpg)\n\n\n<style> .slide_p4 { background: linear-gradient(to bottom, #11998e, #38ef7d); } </style>\n\n-----------------\n## Page with image background\n* containing text and a table\n\n| Left aligned | Middle aligned | Right aligned |\n| :------------------- | :----------------------: | --------------------: |\n| Test               | Test                    | Test                |\n| Test               | Test                    | Test                |\n\n\n\n<style> \n.slide_p5 { background: url('file:///android_asset/img/schindelpattern.jpg') no-repeat center center fixed; background-size: cover; }\n.slide_p5 > .slide_body > * { color: black; }\n</style>\n\n-----------------\n";
            }
            case 6: {
                return "Content-Type: text/x-zim-wiki\nWiki-Format: zim 0.4\nCreation-Date: 2019-01-28T20:53:47+01:00\n\n====== Zim Wiki ======\nLet me try to gather a list of the formatting options Zim provides.\n\n====== Head 1 ======\n\n===== Head 2 =====\n\n==== Head 3 ====\n\n=== Head 4 ===\n\n== Head 5 ==\n\n**Bold**\n//italics//\n__marked (yellow Background)__\n~~striked~~\n\n* Unordered List\n* second item\n	* [[Sub-Item]]\n		* Subsub-Item\n			* and one more sub\n* Back to first indent level\n\n1. ordered list\n2. second item\n	a. item 2a\n		1. Item 2a1\n		2. Item 2a2\n	b. item 2b\n		1. 2b1\n			a. 2b1a\n3. an so on...\n\n[ ] Checklist\n[ ] unchecked item\n[*] checked item\n[x] crossed item\n[>] Item marked with a yellow left-to-right-arrow\n[ ] another unchecked item\n\n\nThis ist ''preformatted text'' inline.\n\n'''\nThis is a preformatted text block.\nIt spans multiple lines.\nAnd it's visually indented.\n'''\n\nWe also have _{subscript} and ^{superscript}.\n\nIt seems there is no way to combine those styles.\n//**this is simply italic**// and you can see the asterisks.\n**//This is simply bold//** and you can see the slashes.\n__**This is simply marked yellow**__ and you can see the asterisks.\n\nThis is a web link: [[https://github.com/gsantner/markor|Markor on Github]]\nLinks inside the Zim Wiki project can be made by simply using the [[Page Name]] in double square brackets.\nThis my also contain some hierarchy information, like [[Folder:Subfolder:Document Name]]\n\n\nThis zim wiki reference file was created for the [[https://github.com/gsantner/markor|Markor]] project by [[https://github.com/gsantner|Gregor Santner]] and is licensed [[https://creativecommons.org/publicdomain/zero/1.0/legalcode|Creative Commons Zero 1.0]] (public domain). File revision 1.";
            }
            case 8: {
                final String header = TextViewUtils.interpolateEscapedDateTime("---\ntags: []\ncreated: '`yyyy-MM-dd`'\ntitle: ''\n---\n\n");
                if (basedir != null && new File(basedir.getParentFile(), ".notabledir").exists()) {
                    return header.replace("created:", "modified:");
                }
                return header;
            }
            case 9: {
                return "source:\ncategory:\ntag:\n------------\n";
            }
            case 10: {
                return "= My Title\n:page-subtitle: This is a subtitle\n:page-last-updated: 2029-01-01\n:page-tags: ['AsciiDoc', 'Markor', 'open source']\n:toc: auto\n:toclevels: 2\n// :page-description: the optional description\n// This should match the structure on the jekyll server:\n:imagesdir: ../assets/img/blog\n\nifndef::env-site[]\n\n// on the jekyll server, the :page-subtitle: is displayed below the title.\n// but it is not shown, when rendered in html5, and the site is rendered in html5, when working locally\n// so we show it additionally only, when we work locally\n// https://docs.asciidoctor.org/asciidoc/latest/document/subtitle/\n\n[discrete] \n=== {page-subtitle}\n\nendif::env-site[]\n\n// local testing:\n:imagesdir: ../app/src/main/assets/img\n\nimage::flowerfield.jpg[]\n\nimage::schindelpattern.jpg[Schindelpattern,200]\n\nbefore inline picture image:schindelpattern.jpg[Schindelpattern,50] and after inline picture\n";
            }
            case 11: {
                // sample.csv
                return "# this is a comment in csv file\n" +
                        "\n" +
                        "# below is the header\n" +
                        "number;text;finishing date\n" +
                        "\n" +
                        "# csv can contain markdown formatting\n" +
                        "1;Learn to use **Markor** formatting\n" +
                        "\n" +
                        "# use \"...\" if the column contains reserved chars\n" +
                        "2;\"Use Delimitter chars like \"\";    :,\";2019-06-24\n" +
                        "\n" +
                        "# use \"...\" if the column is multiline\n" +
                        "3;\"multi\n" +
                        "   line\";2059-12-24\n";
            }
            case 12: {
                // orgmode
                return "OrgMode Reference\n" + "* Headline\n" + "** Nested headline\n" + "*** Deeper\n" + "\n" + "* Basic markup\n" + "This is the general building block for org-mode navigation.\n" + "- _underscores let you underline things_\n" + "- *stars add emphasis*\n" + "- /slashes are italics/\n" + "- +pluses are strikethrough+\n" + "- =equal signs are verbatim text=\n" + "- ~tildes can also be used~\n" + "\n" + "* List\n" + "** Unordered List\n" + "- Item 1\n" + "- Item 2\n" + "  - Subitem 2.1\n" + "  - Subitem 2.2\n" + "** Ordered List\n" + "1. First Item\n" + "2. Second Item\n" + "   1. Subitem 2.1\n" + "   2. Subitem 2.2\n" + "- [X] Completed Task\n" + "- [ ] Uncompleted Task\n" + "** Nested List\n" + "   - Item A\n" + "     - Subitem A.1\n" + "     - Subitem A.2\n" + "   - Item B\n" + "\n" + "* Tables\n" + "\n" + "| First Name                 | Last Name           | Years using Emacs |\n" + "|----------------------------+---------------------+-------------------|\n" + "| Lee                        | Hinman              |                 5 |\n" + "| Mike                       | Hunsinger           |                 2 |\n" + "| Daniel                     | Glauser             |                 4 |\n" + "| Really-long-first-name-guy | long-last-name-pers |                 1 |\n" + "\n" + "* Org-mode links\n" + "\n" + "#+BEGIN_SRC fundamental\n" + "[[http://google.com/][Google]]\n" + "#+END_SRC\n" + "\n" + "[[./images/pic1.png]]\n" + "\n" + "\n" + "* TODO List\n" + "** TODO This is a task that needs doing\n" + "** TODO Another todo task\n" + "- [ ] sub task one\n" + "- [X] sub task two\n" + "- [ ] sub task three\n" + "** DONE I've already finished this one\n" + "*** CANCELLED learn todos\n" + "    CLOSED: [2023-10-16 Mon 08:39]\n" + "\n" + "* Code\n" + "#+BEGIN_LaTeX\n" + "$a + b$\n" + "#+END_LaTeX\n" + "\n" + "#+BEGIN_SRC emacs-lisp\n" + "(defun my/function ()\n" + "  \"docstring\"\n" + "  (interactive)\n" + "  (progn\n" + "    (+ 1 1)\n" + "    (message \"Hi\")))\n" + "#+END_SRC\n" + "\n";
            }
        }
        return null;
    }

    private Pair<byte[], Integer> getTemplateContent(
            final Spinner templateSpinner,
            final File basedir,
            final String filename,
            final boolean encrypt
    ) {

        String template = getTemplateByPos(templateSpinner.getSelectedItemPosition(), filename, basedir);
        if (template == null) {
            final Map<String, File> snippets = MarkorDialogFactory.getSnippets(ApplicationObject.settings());
            final Object sel = templateSpinner.getSelectedItem();
            if (sel instanceof String && snippets.containsKey((String) sel)) {
                final String t = GsFileUtils.readTextFileFast(snippets.get((String) sel)).first;
                final String title = GsFileUtils.getNameWithoutExtension(filename);
                template = TextViewUtils.interpolateSnippet(t, title, "");
            }
        }

        if (template == null) {
            return Pair.create(null, -1);
        }

        final int startingIndex = template.indexOf(HighlightingEditor.PLACE_CURSOR_HERE_TOKEN);
        template = template.replace(HighlightingEditor.PLACE_CURSOR_HERE_TOKEN, "");

        // Has no utility in a new file
        template = template.replace(HighlightingEditor.INSERT_SELECTION_HERE_TOKEN, "");

        final byte[] bytes;
        if (encrypt && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final char[] pass = ApplicationObject.settings().getDefaultPassword();
            bytes = new JavaPasswordbasedCryption(Build.VERSION.SDK_INT, new SecureRandom()).encrypt(template, pass);
        } else {
            bytes = template.getBytes();
        }

        return Pair.create(bytes, startingIndex);
    }
}