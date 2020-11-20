/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dhbw.rahmlab.nativelibloader.impl;

import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.DynamicLibraryBundle;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.os.DynamicLibraryBundleInfo;
import de.dhbw.rahmlab.nativelibloader.impl.com.jogamp.common.util.RunnableExecutor;
import java.util.ArrayList;
import java.util.List;

public class BundleInfoImpl implements DynamicLibraryBundleInfo {

    private final List<String> glueLibNames;

    public BundleInfoImpl(List<String> glueLibNames) {
        this.glueLibNames = glueLibNames;
    }

    //Die Funktionalität gibt es nur für ToolLibraries
    /**
     * <p>
     * Returns <code>true</code>, since we might load the library and allow
     * symbol access to subsequent libs.
     * </p>
     */
    @Override
    public final boolean shallLinkGlobal() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns <code>false</code>.
     * </p>
     */
    @Override
    public final boolean shallLookupGlobal() {
        return false;
    }

    //Only the last entry is crucial, ie all other are optional preload dependencies and may generate errors, which are ignored.
    //GlueLibs sind die .so Dateien, die gesucht werden sollen.
    @Override
    public final List<String> getGlueLibNames() {
        return glueLibNames;
    }

    @Override
    public final boolean searchToolLibInSystemPath() {
        return false;
    }

    @Override
    public final boolean searchToolLibSystemPathFirst() {
        return false;
    }

    //ToolLibs sind Bibliotheken auf dem System, die für die zu ladenden Bibliotheken gebraucht werden.
    @Override
    public final List<List<String>> getToolLibNames() {
        final List<List<String>> libNamesList = new ArrayList<>();

        return libNamesList;
    }

    @Override
    public final List<String> getToolGetProcAddressFuncNameList() {
        return null;
    }

    @Override
    public final long toolGetProcAddress(final long toolGetProcAddressHandle, final String funcName) {
        return 0;
    }

    @Override
    public final boolean useToolGetProcAdressFirst(final String funcName) {
        return false;
    }

    @Override
    public final RunnableExecutor getLibLoaderExecutor() {
        return DynamicLibraryBundle.getDefaultRunnableExecutor();
    }
}
