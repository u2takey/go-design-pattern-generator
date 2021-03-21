package actions;


import com.goide.psi.GoFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import template.pattern.ConvertTemplate;
import utils.PopupUtil;

public class ConvertAction extends BaseAction {
    @Override
    protected void actionPerformedImpl(AnActionEvent event, Project project, GoFile file,
                                       Editor editor) {
        PopupUtil.getChooseStructPopup(file, editor, project, "Choose struct from", structType1 -> {
            PopupUtil.getChooseClassPopup(file, project, "Choose struct to", structType2 -> {
                new ConvertTemplate(event, structType1, structType2).generateText();
            });
        });
    }
}
