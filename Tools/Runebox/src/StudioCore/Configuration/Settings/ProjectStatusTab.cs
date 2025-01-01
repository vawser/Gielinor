﻿using ImGuiNET;
using StudioCore.Editor;
using StudioCore.Interface;
using StudioCore.Utilities;
using System.Collections.Generic;
using System.Diagnostics;
using System.Numerics;

namespace StudioCore.Configuration.Settings;

public class ProjectStatusTab
{
    public ProjectStatusTab()
    {

    }

    public void Display()
    {
        var widthUnit = ImGui.GetWindowWidth();

        if (Runebox.ProjectHandler.CurrentProject == null)
        {
            ImGui.Text("No project loaded");
            UIHelper.ShowHoverTooltip("No project has been loaded yet.");
        }
        else if (TaskManager.AnyActiveTasks())
        {
            ImGui.Text("Waiting for program tasks to finish...");
            UIHelper.ShowHoverTooltip("Warbox must finished all program tasks before it can load a project.");
        }
        else
        {
            ImGui.Text($"Project Name: {Runebox.ProjectHandler.CurrentProject.Config.ProjectName}");
            ImGui.Text($"Project Type: {Runebox.ProjectType}");
            ImGui.Text($"Project Root Directory: {Runebox.GameRoot}");
            ImGui.Text($"Project Mod Directory: {Runebox.ProjectRoot}");

            ImGui.Separator();

            if (ImGui.Button("Open Project.JSON", new Vector2(widthUnit * 0.5f, 32)))
            {
                var projectPath = CFG.Current.LastProjectFile;
                Process.Start("explorer.exe", projectPath);
            }
            ImGui.SameLine();
            if (ImGui.Button("Clear Recent Project List", new Vector2(widthUnit * 0.5f, 32)))
            {
                CFG.Current.RecentProjects = new List<CFG.RecentProject>();
                CFG.Save();
            }

            ImGui.Separator();
        }
    }
}
