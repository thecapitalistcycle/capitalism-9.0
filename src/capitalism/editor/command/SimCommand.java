/*
 * Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/package capitalism.editor.command;

import capitalism.editor.EditorManager;
import capitalism.view.command.DisplayCommand;
import capitalism.view.custom.ImageButton;

/**
 * This class, invoked via {@code EditorControlBar}, closes the editor window and gives control to the 
 * main simulation window.
 *  */

public class SimCommand implements DisplayCommand {
	@Override public void execute(ImageButton caller) {
		EditorManager.closeEditorWindow();
	}
}
