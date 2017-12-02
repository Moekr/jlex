package com.moekr.jlex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

class DfaGenerator {
    private final Log logger;
    private final DataSet dataSet;

    private List<List<Transition>> groupList;
    private int[] groupIndex;

    DfaGenerator(){
        logger = LogFactory.getLog(this.getClass());
        dataSet = DataSet.getInstance();

        groupList = new ArrayList<>();
    }

    void generate(){
        logger.info("Generating deterministic finite automaton(DFA)...");
        makeTransition();
        logger.info("Generated " + dataSet.getDfaStateList().size() + " DFA states.");
        logger.info("Simplifying deterministic finite automaton(DFA)...");
        simplify();
        logger.info("Simplified DFA to " + groupList.size() + " states.");
    }

    //生成DFA状态及转换关系
    private void makeTransition(){
        dataSet.setStateToTransitionMap(new int[dataSet.getRuleList().size()]);

        Bunch bunch = new Bunch();
        for(int i = 0; i < dataSet.getRuleList().size(); i++){
            bunch.getNfaStateList().clear();
            bunch.getNfaStateList().addAll(dataSet.getRuleList().get(i));

            bunch.getNfaStateSet().clear();
            bunch.getNfaStateList().forEach(nfaState -> bunch.getNfaStateSet().set(nfaState.getLabel()));

            closure(bunch);
            addToDfaStateList(bunch);

            dataSet.getStateToTransitionMap()[i] = dataSet.getTransitionList().size();

            DfaState dfaState;
            while((dfaState = dataSet.getDfaStateList().stream().filter(dfa -> !dfa.isMark()).findFirst().orElse(null)) != null){
                dfaState.setMark(true);

                Transition transition = Factory.createTransition();
                transition.setAction(dfaState.getAction());
                transition.setAnchor(dfaState.getAnchor());

                for (int character = 0; character < dataSet.getRange(); character++) {
                    bunch.getNfaStateList().clear();
                    bunch.getNfaStateSet().clear();

                    for(NfaState state : dfaState.getNfaStateList()){
                        if (state.getEdge() == character || (state.getEdge() == Constant.CHARACTER_CLASS && state.getCharacterSet().contains(character))) {
                            bunch.getNfaStateList().add(state.getNext()[0]);
                            bunch.getNfaStateSet().set(state.getNext()[0].getLabel());
                        }
                    }
                    if(!bunch.getNfaStateList().isEmpty()){
                        closure(bunch);
                    }

                    int nextState;
                    if(bunch.getNfaStateList().isEmpty()){
                        nextState = Constant.FINAL;
                    }else{
                        nextState = ifInDfaStateList(bunch);
                        if(nextState == Constant.NOT_IN_DFA_STATES){
                            nextState = addToDfaStateList(bunch);
                        }
                    }
                    transition.getMoveMap()[character] = nextState;
                }
                dataSet.getTransitionList().add(transition);
            }
        }
    }

    //计算NFA状态闭包
    private void closure(Bunch bunch){
        bunch.setAction(null);
        bunch.setActionIndex(Integer.MAX_VALUE);
        bunch.setAnchor(Constant.NONE);
        bunch.getNfaStateList().sort(Comparator.comparingInt(NfaState::getLabel));

        Stack<NfaState> nfaStateStack = new Stack<>();
        bunch.getNfaStateList().forEach(nfaStateStack::push);

        NfaState nfaState;
        while (!nfaStateStack.empty()){
            nfaState = nfaStateStack.pop();
            if (nfaState.getAction() != null && nfaState.getLabel() < bunch.getActionIndex()) {
                bunch.setAction(nfaState.getAction());
                bunch.setActionIndex(nfaState.getLabel());
                bunch.setAnchor(nfaState.getAnchor());
            }
            if(nfaState.getEdge() == Constant.EPSILON){
                for (int i = 0;i < nfaState.getNext().length;i++) {
                    if (nfaState.getNext()[i] != null) {
                        if (!bunch.getNfaStateList().contains(nfaState.getNext()[i])) {
                            bunch.getNfaStateSet().set(nfaState.getNext()[i].getLabel());
                            bunch.getNfaStateList().add(nfaState.getNext()[i]);
                            nfaStateStack.push(nfaState.getNext()[i]);
                        }
                    }
                }
            }
        }

        bunch.getNfaStateList().sort(Comparator.comparingInt(NfaState::getLabel));
    }

    //查询当前NFA状态集是否存在对应DFA状态
    private int ifInDfaStateList(Bunch bunch){
        DfaState dfaState = dataSet.getDfaStateMap().get(bunch.getNfaStateSet());
        if(dfaState != null){
            return dfaState.getLabel();
        }
        return Constant.NOT_IN_DFA_STATES;
    }

    //添加当前NFA状态集为新的DFA状态
    private int addToDfaStateList(Bunch bunch){
        DfaState dfaState = Factory.createDfaState();
        dfaState.setNfaStateList(new ArrayList<>(bunch.getNfaStateList()));
        dfaState.setNfaStateSet((BitSet) bunch.getNfaStateSet().clone());
        dfaState.setAction(bunch.getAction());
        dfaState.setAnchor(bunch.getAnchor());
        dataSet.getDfaStateMap().put(dfaState.getNfaStateSet(), dfaState);
        return dfaState.getLabel();
    }

    private void simplify(){
        initGroup();
        int count = groupList.size();
        int oldCount = count - 1;
        while (oldCount != count){
            oldCount = count;
            for (int i = 0; i < count; i++) {
                List<Transition> group = groupList.get(i);
                if(group.size() <= 1){
                    continue;
                }
                List<Transition> newGroup = new ArrayList<>();
                boolean added = false;
                Transition inGroup = group.get(0);
                for (int transitionIndex = 1; transitionIndex < group.size(); transitionIndex++) {
                    Transition current = group.get(transitionIndex);
                    for (int character = 0; character < dataSet.getRange(); character++) {
                        int targetInGroup = inGroup.getMoveMap()[character];
                        int targetCurrent = current.getMoveMap()[character];
                        if (targetInGroup != targetCurrent && (targetInGroup == Constant.FINAL || targetCurrent == Constant.FINAL || groupIndex[targetInGroup] != groupIndex[targetCurrent])) {
                            group.remove(transitionIndex);
                            transitionIndex--;
                            newGroup.add(current);
                            if (!added) {
                                groupList.add(newGroup);
                                added = true;
                                count++;
                            }
                            groupIndex[current.getLabel()] = groupList.size() - 1;
                            break;
                        }
                    }
                }
            }
        }
        fixTransition();
        reduce();
    }

    private void initGroup(){
        groupIndex = new int[dataSet.getTransitionList().size()];

        for (int i = 0; i < dataSet.getTransitionList().size(); i++) {
            Transition current = dataSet.getTransitionList().get(i);
            boolean found = false;
            for (int j = 0; j < groupList.size(); j++) {
                List<Transition> group = groupList.get(j);
                Transition inGroup = group.get(0);
                if(Objects.equals(inGroup.getAction(), current.getAction())){
                    group.add(current);
                    groupIndex[i] = j;
                    found = true;
                    break;
                }
            }
            if(!found){
                List<Transition> newGroup = new ArrayList<>();
                newGroup.add(current);
                this.groupIndex[i] = groupList.size();
                groupList.add(newGroup);
            }
        }
    }

    private void fixTransition() {
        for (int i = 0; i < dataSet.getStateToTransitionMap().length; i++) {
            if (dataSet.getStateToTransitionMap()[i] != Constant.FINAL) {
                dataSet.getStateToTransitionMap()[i] = groupIndex[dataSet.getStateToTransitionMap()[i]];
            }
        }
        List<Transition> newList = new ArrayList<>();
        for(List<Transition> group : groupList){
            Transition transition = group.get(0);
            newList.add(transition);
            for (int character = 0;character < dataSet.getRange();character++){
                if (transition.getMoveMap()[character] != Constant.FINAL) {
                    transition.getMoveMap()[character] = this.groupIndex[transition.getMoveMap()[character]];
                }
            }
        }
        dataSet.setTransitionList(newList);
    }

    private void reduce(){
        BitSet bitSet = new BitSet();
        dataSet.setAnchorArray(new int[dataSet.getTransitionList().size()]);
        for (int i = 0; i < dataSet.getTransitionList().size(); i++) {
            Transition transition = dataSet.getTransitionList().get(i);
            dataSet.getActionList().add(transition.getAction());
            dataSet.getAnchorArray()[i] = transition.getAnchor();
        }

        dataSet.setColumnMap(new int[dataSet.getRange()]);
        for (int i = 0; i < dataSet.getColumnMap().length; i++) {
            dataSet.getColumnMap()[i] = -1;
        }
        int reducedColumn;
        for (reducedColumn = 0;;reducedColumn++) {
            int i;
            for (i = reducedColumn;i < dataSet.getRange();i++) {
                if (dataSet.getColumnMap()[i] == -1) {
                    break;
                }
            }
            if(i >= dataSet.getRange()){
                break;
            }
            bitSet.set(i);
            dataSet.getColumnMap()[i] = reducedColumn;
            for (int j = i + 1;j < dataSet.getRange();j++) {
                if (dataSet.getColumnMap()[j] == -1 && columnEquals(i,j)){
                    dataSet.getColumnMap()[j] = reducedColumn;
                }
            }
        }
        for (int i = 0; i < dataSet.getRange();i++) {
            if (bitSet.get(i)) {
                bitSet.clear(i);
                int j = dataSet.getColumnMap()[i];
                if (i == j) {
                    continue;
                }
                copyColumn(j,i);
            }
        }
        dataSet.setRange(reducedColumn);
        truncateColumn();

        int rows = dataSet.getTransitionList().size();
        dataSet.setRowMap(new int[rows]);
        for (int i = 0;i < rows;i++) {
            dataSet.getRowMap()[i] = -1;
        }
        int reducedRow;
        for (reducedRow = 0;;reducedRow++) {
            int i;
            for (i = reducedRow;i < rows;i++) {
                if (dataSet.getRowMap()[i] == -1) {
                    break;
                }
            }
            if (i >= rows) {
                break;
            }
            bitSet.set(i);
            dataSet.getRowMap()[i] = reducedRow;
            for (int j = i + 1;j < rows;j++) {
                if (dataSet.getRowMap()[j] == -1 && rowEquals(i,j)) {
                    dataSet.getRowMap()[j] = reducedRow;
                }
            }
        }
        for (int i = 0;i < rows;i++) {
            if (bitSet.get(i)) {
                bitSet.clear(i);
                int j = dataSet.getRowMap()[i];
                if (i == j) {
                    continue;
                }
                copyRow(j,i);
            }
        }
        while (dataSet.getTransitionList().size() != reducedRow){
            if(dataSet.getTransitionList().size() > reducedRow){
                dataSet.getTransitionList().remove(reducedRow);
            }else {
                dataSet.getTransitionList().add(null);
            }
        }
    }

    private boolean columnEquals(int i, int j){
        for (int k = 0; k < dataSet.getTransitionList().size(); k++) {
            Transition transition = dataSet.getTransitionList().get(k);
            if (transition.getMoveMap()[i] != transition.getMoveMap()[j]) {
                return false;
            }
        }
        return true;
    }

    private void copyColumn(int dest, int src){
        for (int i = 0; i < dataSet.getTransitionList().size(); i++) {
            Transition transition = dataSet.getTransitionList().get(i);
            transition.getMoveMap()[dest] = transition.getMoveMap()[src];
        }
    }

    private void truncateColumn(){
        for (Transition transition : dataSet.getTransitionList()) {
            int[] moveMap = new int[dataSet.getRange()];
            System.arraycopy(transition.getMoveMap(), 0, moveMap, 0, moveMap.length);
            transition.setMoveMap(moveMap);
        }
    }

    private boolean rowEquals(int i, int j){
        Transition a = dataSet.getTransitionList().get(i),b = dataSet.getTransitionList().get(j);
        for (int k = 0; k < dataSet.getRange(); k++) {
            if (a.getMoveMap()[k] != b.getMoveMap()[k]) {
                return false;
            }
        }
        return true;
    }

    private void copyRow(int dest, int src){
        Transition transition = dataSet.getTransitionList().get(src);
        dataSet.getTransitionList().set(dest, transition);
    }
}
