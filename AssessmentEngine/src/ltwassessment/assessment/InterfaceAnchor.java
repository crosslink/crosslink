package ltwassessment.assessment;

import java.util.Vector;

public interface InterfaceAnchor {

	IndexedAnchor getParent();

	String offsetToString();

	String statusToString();

	int getStatus();

	void markAllLinksIrrevlent();

	void setStatus(int relevant);

	String lengthToString();

	String screenPosStartToString();

	String screenPosEndToString();

	String extendedLengthToString();

	String offsetIndexToString();

	String lengthIndexToString();

	int getScreenPosStart();

	int getScreenPosEnd();

	int checkStatus();

	Bep getNextLink(Bep currentLink, boolean nextUnassessed);

	Bep getPreviousLink(Bep currentLink, boolean nextUnassessed);

	Vector<Bep> getBeps();

	String getName();

}
