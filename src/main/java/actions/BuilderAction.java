package actions;

import com.goide.psi.GoFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import template.pattern.BuilderTemplate;
import utils.PopupUtil;

/**
 * @author DASK
 * @date 2020/3/27 15:54
 * @description //TODO 建造者模式操作
 */
public class BuilderAction extends BaseAction {
    @Override
    protected void actionPerformedImpl(AnActionEvent event, Project project, GoFile file,
                                       Editor editor) {
        PopupUtil.getChooseStructPopup(file, editor, project, null,
                goTypeSpec -> new BuilderTemplate(event, goTypeSpec).generateText());
    }
}
