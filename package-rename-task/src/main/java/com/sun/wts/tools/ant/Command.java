/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.wts.tools.ant;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * One replace command.
 *
 * @author Kohsuke Kawaguchi
 */
public class Command {
    private final Pattern from;
    private final String to;
    private final List<Pattern> excludes;

    public Command(Pattern from, String to) {
        this(from,to, Collections.<Pattern>emptyList());
    }

    public Command(Pattern from, String to, List<Pattern> excludes) {
        this.from = from;
        this.to = to;
        this.excludes = excludes;
    }

    /**
     * Replaces all the occurrences of the given pattern and returns it.
     */
    public String replace(String input) {
        Matcher m = from.matcher(input);

        if (!m.find()) return input;

        boolean result;
        StringBuffer sb = new StringBuffer();
        do {
            // make sure this doesn't match the excluded patterns
            boolean excluded=false;
            for (Pattern p : excludes) {
                if(p.matcher(input.substring(m.start())).lookingAt()) {
                    excluded=true;
                    break;
                }
            }
            if(excluded)
                m.appendReplacement(sb,m.group()); // don't replace this
            else
                m.appendReplacement(sb,to);
            result = m.find();
        } while (result);
        m.appendTail(sb);
        return sb.toString();
    }
}
