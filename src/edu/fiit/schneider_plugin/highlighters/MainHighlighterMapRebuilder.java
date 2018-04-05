package edu.fiit.schneider_plugin.highlighters;

import com.google.common.collect.Lists;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import edu.fiit.schneider_plugin.entity.Change;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainHighlighterMapRebuilder {
    private static MainHighlighterMapRebuilder instance = null;

    public static MainHighlighterMapRebuilder getInstance() {
        if (instance == null) {
            instance = new MainHighlighterMapRebuilder();
        }
        return instance;
    }

    public void rebuildMapPlus(Change change, Project project) {
        //noinspection ConstantConditions
        MarkupModel model = FileEditorManager.getInstance(project).getSelectedTextEditor().getMarkupModel();
        String modelString = model.toString();
        Map<String, RangeHighlighter> map = MainHighlighter.getInstance().getHighlighters().get(modelString);
        List<String> keyList = new ArrayList<>(map.keySet());
        keyList = Lists.reverse(keyList);
        updateMap(change, keyList, model.toString());
    }

    public void rebuildMapMinus(Change change, Project project) {
        //noinspection ConstantConditions
        MarkupModel model = FileEditorManager.getInstance(project).getSelectedTextEditor().getMarkupModel();
        String modelString = model.toString();
        Map<String, RangeHighlighter> map = MainHighlighter.getInstance().getHighlighters().get(modelString);
        List<String> keyList = new ArrayList<>(map.keySet());

        updateMap(change, keyList, model.toString());

    }

    private void updateMap(Change change, List<String> keyList, String modelString) {
        Map<String, RangeHighlighter> map = MainHighlighter.getInstance().getHighlighters().get(modelString);
        StringBuilder keyBuilder = new StringBuilder();
        for (String key : keyList) {
            String[] arr = key.split(" ");
            int fromLine = Integer.parseInt(arr[0]);
            int toLine = Integer.parseInt(arr[1]);
            if (change.getLineAt() <= fromLine || change.getLineAt() <= toLine) {
                //mensia ako oboje -> pred celym blokom
                if (change.getLineAt() <= fromLine) {
                    keyBuilder.append(fromLine + change.getLineCount()).append(" ");
                    keyBuilder.append(toLine + change.getLineCount());
                }
                //mensia ako toLine ale vacsia ako fromLine -> vlozil sa enter do vnutra highlighteru
                else if (change.getLineAt() <= toLine) {
                    keyBuilder.append(fromLine).append(" ");
                    keyBuilder.append(toLine + change.getLineCount());
                }
                RangeHighlighter removedHighlighter = map.remove(key);
                map.put(keyBuilder.toString(), removedHighlighter);
                keyBuilder.setLength(0);
            }

        }
    }
}
