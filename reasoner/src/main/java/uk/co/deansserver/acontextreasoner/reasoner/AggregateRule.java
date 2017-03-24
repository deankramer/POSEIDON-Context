/*
 * Copyright 2017 aContextReasoner Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.deansserver.acontextreasoner.reasoner;

import android.util.Log;

import org.prop4j.Equals;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.NodeReader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for holding aggregate rules, and handling temporal literals.
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class AggregateRule {

    private int mDateIncrement = 86400000;
    public final static String[] propSymbols = new String[] { "iff",
            "implies", "or", "and", "not", "(", ")" };

    private String mPropRule;
    private Node mPropNodes;
    private String mRule;

    //          Literal, Temporal Element
    private Map<String, TemporalValue> mTemporalLiterals;
    private Map<String, Literal> mCachedLiterals;
    private ArrayList<String> mLiterals;
    private ArrayList<String> mCachibleLiterals;
    private Literal mAggregateState;
    private Set<String> mInterestedContexts;
    private String mContextName;


    public AggregateRule(String rule) {
        mLiterals = new ArrayList<>();
        mCachibleLiterals = new ArrayList<>();
        mCachedLiterals = new HashMap<>();
        mTemporalLiterals = new HashMap<>();
        mInterestedContexts = new HashSet<>();

        mRule = rule;
        mPropRule = checkForTemporalLiterals(rule);

        NodeReader reader = new NodeReader();

        mPropNodes = reader.stringToNode(mPropRule);

        setStateLiteral();
        setContextName();

        List<Node> literals = mPropNodes.getAllNodeTypes(Literal.class);

        for (Node literal : literals) {
            String literalString = (String) ((Literal)literal).var;

            TemporalValue temp = mTemporalLiterals.get(literalString);

            if (temp == null) {
                if (! literalString.equalsIgnoreCase((String) mAggregateState.var)) {
                    mLiterals.add(literalString);
                }
            }

            addInterestedContext(literalString);
        }

    }

    private void setContextName() {
        String name = (String) mAggregateState.var;
        String[] splittedValues = name.split("_");
        mContextName = splittedValues[0];
    }

    public String getContextName() {
        return mContextName;
    }

    private void addInterestedContext(String literalString) {

        if (! literalString.equalsIgnoreCase((String) mAggregateState.var)) {
            String[] splittedValues = literalString.split("_");
            String contextName = splittedValues[0];
            mInterestedContexts.add(contextName);
        }
    }

    public void addCachedLiteral(Literal literal) {

        String name = (String) literal.var;
        mCachedLiterals.put(name, literal);
        mTemporalLiterals.remove(name);
    }

    public boolean isAffectedBy(String contextState) {

        for (String context : mInterestedContexts) {
            if (context.equals(contextState)) {
                return true;
            }
        }

        return false;
    }

    private void setStateLiteral() {

        List<Node> nodes = mPropNodes.getAllNodeTypes(Equals.class);

        if (nodes.size() == 1) {
            Node[] children = nodes.get(0).getChildren();

            for (Node child : children) {
                if (child instanceof Literal) {
                    mAggregateState = (Literal) child.clone();
                    mAggregateState.positive = true;
                }
            }
        }

    }

    public TemporalValue getTemporalValue(String literalName) {
        return mTemporalLiterals.get(literalName);
    }

    private TemporalValue parseTemporalValues(String literalName, String value) {
        TemporalValue tempValue = new TemporalValue();

        if (value.contains("#")) {
            tempValue.mStrong = true;
            value = value.replace("#", "");
        }

        String[] splittedValues = value.split("-");

        tempValue.mStartTimeString = splittedValues[0];

        if (splittedValues.length == 2) {
            tempValue.mEndTimeString = splittedValues[1];
        }

        try {
            if (tempValue.parseTemporalValues()){
                if (tempValue.mEndTime < System.currentTimeMillis()) {
                    mCachibleLiterals.add(literalName);
                }
            }
        } catch (ParseException ex) {
            Log.e("Aggregate Rule", "Cannot parse: " + tempValue.mStartTimeString
                    + " +  " + tempValue.mEndTimeString);
        }

        return tempValue;
    }


    public boolean requiresTemporalValueUpdates() {

        for (TemporalValue temporalValue : mTemporalLiterals.values()) {
            if (temporalValue.mStartUpdate || temporalValue.mEndUpdate) {
                return true;
            }
        }

        return false;
    }

    public void incrementTemporalValueDates() {

        for (TemporalValue tempValue : mTemporalLiterals.values()) {
            if (tempValue.mStartUpdate) {
                tempValue.mStartTime += mDateIncrement;
            }

            if (tempValue.mEndUpdate) {
                tempValue.mEndTime += mDateIncrement;
            }

        }
    }

    public Map<String, TemporalValue> getTemporalLiterals() {
        return mTemporalLiterals;
    }

    public List<String> getInstanceLiterals() {
        return mLiterals;
    }

    public List<String> getAllLiterals() {

        ArrayList<String> result = new ArrayList<>(mLiterals);
        result.addAll(mTemporalLiterals.keySet());

        return result;
    }

    public Node getPropNodes() {
        return mPropNodes;
    }

    public String getRule() {
        return mRule;
    }

    private String checkForTemporalLiterals(String rule) {
        rule = insertWhitespacesAtBrackets(rule);
        rule = reduceWhiteSpaces(rule);

        while (rule.contains("] ")) {

            int indEnd = rule.indexOf("] ");
            int indStart = rule.substring(0,indEnd).lastIndexOf("[");

            String temporalValue = rule.substring(indStart + 1, indEnd).trim();

            String temp = rule.substring(0, indStart - 1);

            String[] splittedString = temp.split(" ");

            int length = splittedString.length;
            StringBuilder sb = new StringBuilder();

            for (int i= length - 1; i >= 0; i--) {

                String sub = splittedString[i];

                if (sub.equals(propSymbols[0]) || sub.equals(propSymbols[1]) ||
                        sub.equals(propSymbols[2]) || sub.equals(propSymbols[3]) ||
                        sub.equals(propSymbols[4]) || sub.equals(propSymbols[5]) ||
                        sub.equals(propSymbols[6])) {
                    break;

                } else {
                    if (sb.length() > 0) {
                        sb.insert(0, " ");
                    }

                    sb.insert(0, sub);
                }
            }

            String literalName = sb.toString();

            mTemporalLiterals.put(literalName, parseTemporalValues(literalName, temporalValue));
            rule = removeCharRange(rule, indStart, indEnd);
        }

        return rule;
    }

    private static String removeCharRange(String s, int start, int end) {
        int diff = end - start;
        StringBuilder sb = new StringBuilder(s.length() - diff);
        sb.append(s.substring(0, start - 1)).append(s.substring(end + 1));
        return sb.toString();
    }

    private static String insertWhitespacesAtBrackets(String str) {
        str = str.replaceAll("\\]", " ] ");
        str = str.replaceAll("\\[", " [ ");
        return str;

    }

    public static String reduceWhiteSpaces(String str) {

        if (str.length() < 2)
            return str;
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(str.charAt(0));
        for (int i = 1; i < str.length(); i++) {
            if (!(Character.isWhitespace(str.charAt(i - 1)) && Character
                    .isWhitespace(str.charAt(i)))) {
                strBuf.append(str.charAt(i));
            }
        }
        return strBuf.toString();
    }

    public List<Literal> getCachedLiterals() {

        List<Literal> cachedLiterals =  new ArrayList<>(mCachedLiterals.values());
        cachedLiterals.add(mAggregateState);

        return cachedLiterals;
    }

    public String getStateName() {
        return (String) mAggregateState.var;
    }

    public Literal getStateLiteral() { return mAggregateState;}

    @Override
    public String toString() {
        return mRule;
    }

    public List<String> getCachibleLiterals() {
        return mCachibleLiterals;
    }
}
