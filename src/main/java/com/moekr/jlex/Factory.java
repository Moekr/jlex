package com.moekr.jlex;

abstract class Factory {
	private static DataSet DATA_SET = DataSet.getInstance();

	static NfaState createNfaState(){
		NfaState nfaState = new NfaState();
		DATA_SET.getNfaStateList().add(nfaState);
		return nfaState;
	}

	static DfaState createDfaState(){
		DfaState dfaState = new DfaState();
		dfaState.setLabel(DATA_SET.getDfaStateList().size());
		DATA_SET.getDfaStateList().add(dfaState);
		return dfaState;
	}

	static Transition createTransition(){
		Transition transition = new Transition();
		transition.setLabel(DATA_SET.getTransitionList().size());
		transition.setMoveMap(new int[DATA_SET.getRange()]);
		return transition;
	}

	/*
		创建代表换行的表达式为(\r|\r\n|\n)的状态转换

		\r/---------1---------\ \n
		 /           \         \
		S           ε \---------E
		 \                     /
		ε \---------2---------/ \n
	*/

	static NfaPair createNewLineNfaPair(){
		NfaPair newLineNfaPair = new NfaPair();
		NfaState start = createNfaState(), end = createNfaState();

		start.setEdge('\r');
		start.getNext()[0] = createNfaState();
		start.getNext()[1] = createNfaState();

		start.getNext()[0].setEdge('\n');
		start.getNext()[0].getNext()[0] = end;
		start.getNext()[0].getNext()[1] = end;

		start.getNext()[1].setEdge('\n');
		start.getNext()[1].getNext()[0] = end;

		newLineNfaPair.setStart(start);
		newLineNfaPair.setEnd(end);
		return newLineNfaPair;
	}
}
