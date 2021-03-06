/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.search.SearchPattern;
import org.eclipse.wst.jsdt.internal.core.index.EntryResult;
import org.eclipse.wst.jsdt.internal.core.index.Index;

public class SuperTypeReferencePattern extends JavaSearchPattern {

public char[] superQualification;
public char[] superSimpleName;

public char[] pkgName;
public char[] simpleName;
public char[] enclosingTypeName;
public int modifiers;

protected static char[][] CATEGORIES = { SUPER_REF };

public static char[] createIndexKey(
	int modifiers,
	char[] packageName,
	char[] typeName,
	char[][] enclosingTypeNames,
	char[] superTypeName) {

	if (superTypeName == null)
		superTypeName = OBJECT;
	char[] superSimpleName = superTypeName;//CharOperation.lastSegment(superTypeName, '.');
	char[] superQualification = null;
//	if (superSimpleName != superTypeName) {
//		int length = superTypeName.length - superSimpleName.length - 1;
//		superQualification = new char[length];
//		System.arraycopy(superTypeName, 0, superQualification, 0, length);
//	}

	// if the supertype name contains a $, then split it into: source name and append the $ prefix to the qualification
	//	e.g. p.A$B ---> p.A$ + B
	char[] superTypeSourceName = CharOperation.lastSegment(superSimpleName, '$');
	if (superTypeSourceName != superSimpleName) {
		int start = superQualification == null ? 0 : superQualification.length + 1;
		int prefixLength = superSimpleName.length - superTypeSourceName.length;
		char[] mangledQualification = new char[start + prefixLength];
		if (superQualification != null) {
			System.arraycopy(superQualification, 0, mangledQualification, 0, start-1);
			mangledQualification[start-1] = '.';
		}
		System.arraycopy(superSimpleName, 0, mangledQualification, start, prefixLength);
		superQualification = mangledQualification;
		superSimpleName = superTypeSourceName;
	}

	char[] simpleName = CharOperation.lastSegment(typeName, '.');
	char[] simpleNameTemp = new char[simpleName.length + packageName.length + 1];
	System.arraycopy(packageName, 0, simpleNameTemp, 0, packageName.length);
	simpleNameTemp[packageName.length] = '.';
	System.arraycopy(simpleName, 0, simpleNameTemp, packageName.length + 1, simpleName.length);

	simpleName = simpleNameTemp;
	char[] enclosingTypeName = CharOperation.concatWith(enclosingTypeNames, '$');
	if (superQualification != null && CharOperation.equals(superQualification, packageName))
		packageName = ONE_ZERO; // save some space

	// superSimpleName / superQualification / simpleName / enclosingTypeName / typeParameters / packageName / superClassOrInterface classOrInterface modifiers
	int superLength = superSimpleName == null ? 0 : superSimpleName.length;
	int superQLength = superQualification == null ? 0 : superQualification.length;
	int simpleLength = simpleName == null ? 0 : simpleName.length;
	int enclosingLength = enclosingTypeName == null ? 0 : enclosingTypeName.length;
	int packageLength = packageName == null ? 0 : packageName.length;
	char[] result = new char[superLength + superQLength + simpleLength + enclosingLength + packageLength + 9];
	int pos = 0;
	if (superLength > 0) {
		System.arraycopy(superSimpleName, 0, result, pos, superLength);
		pos += superLength;
	}
	result[pos++] = SEPARATOR;
	if (superQLength > 0) {
		System.arraycopy(superQualification, 0, result, pos, superQLength);
		pos += superQLength;
	}
	result[pos++] = SEPARATOR;
	if (simpleLength > 0) {
		System.arraycopy(simpleName, 0, result, pos, simpleLength);
		pos += simpleLength;
	}
	result[pos++] = SEPARATOR;
	if (enclosingLength > 0) {
		System.arraycopy(enclosingTypeName, 0, result, pos, enclosingLength);
		pos += enclosingLength;
	}
	result[pos++] = SEPARATOR;
	if (packageLength > 0) {
		System.arraycopy(packageName, 0, result, pos, packageLength);
		pos += packageLength;
	}
	result[pos++] = SEPARATOR;
	result[pos] = (char) modifiers;
	return result;
}

public SuperTypeReferencePattern(
	char[] superQualification,
	char[] superSimpleName,
	int matchRule) {

	this(matchRule);

	this.superQualification = isCaseSensitive() ? superQualification : CharOperation.toLowerCase(superQualification);
	this.superSimpleName = (isCaseSensitive() || isCamelCase())  ? superSimpleName : CharOperation.toLowerCase(superSimpleName);
	((InternalSearchPattern)this).mustResolve = superQualification != null;
}
SuperTypeReferencePattern(int matchRule) {
	super(SUPER_REF_PATTERN, matchRule);
}
/*
 * superSimpleName / superQualification / simpleName / enclosingTypeName / typeParameters / pkgName / superClassOrInterface classOrInterface modifiers
 */
public void decodeIndexKey(char[] key) {
	int slash = CharOperation.indexOf(SEPARATOR, key, 0);
	this.superSimpleName = CharOperation.subarray(key, 0, slash);

	// some values may not have been know when indexed so decode as null
	int start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	this.superQualification = slash == start ? null : CharOperation.subarray(key, start, slash);

	slash = CharOperation.indexOf(SEPARATOR, key, start = slash + 1);
	this.simpleName = CharOperation.subarray(key, start, slash);

	start = ++slash;
	if (key[start] == SEPARATOR) {
		this.enclosingTypeName = null;
	} else {
		slash = CharOperation.indexOf(SEPARATOR, key, start);
		if (slash == (start+1) && key[start] == ZERO_CHAR) {
			this.enclosingTypeName = ONE_ZERO;
		} else {
			char[] names = CharOperation.subarray(key, start, slash);
			this.enclosingTypeName = names;
		}
	}

	start = ++slash;
	if (key[start] == SEPARATOR) {
		this.pkgName = null;
	} else {
		slash = CharOperation.indexOf(SEPARATOR, key, start);
		if (slash == (start+1) && key[start] == ZERO_CHAR) {
			this.pkgName = this.superQualification;
		} else {
			char[] names = CharOperation.subarray(key, start, slash);
			this.pkgName = names;
		}
	}

	this.modifiers = key[slash + 3]; // implicit cast to int type
}
public SearchPattern getBlankPattern() {
	return new SuperTypeReferencePattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[][] getIndexCategories() {
	return CATEGORIES;
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	SuperTypeReferencePattern pattern = (SuperTypeReferencePattern) decodedPattern;

	if (pattern.superQualification != null)
		if (!matchesName(this.superQualification, pattern.superQualification)) return false;

	return matchesName(this.superSimpleName, pattern.superSimpleName);
}
EntryResult[] queryIn(Index index) throws IOException {
	char[] key = this.superSimpleName; // can be null
	int matchRule = getMatchRule();

	// cannot include the superQualification since it may not exist in the index
	switch(getMatchMode()) {
		case R_EXACT_MATCH :
			if (this.isCamelCase) break;
			// do a prefix query with the superSimpleName
			matchRule &= ~R_EXACT_MATCH;
			matchRule |= R_PREFIX_MATCH;
			if (this.superSimpleName != null)
				key = CharOperation.append(this.superSimpleName, SEPARATOR);
			break;
		case R_PREFIX_MATCH :
			// do a prefix query with the superSimpleName
			break;
		case R_PATTERN_MATCH :
			// do a pattern query with the superSimpleName
			break;
		case R_REGEXP_MATCH :
			// TODO (frederic) implement regular expression match
			break;
	}

	return index.query(getIndexCategories(), key, matchRule); // match rule is irrelevant when the key is null
}
protected StringBuffer print(StringBuffer output) {
		output.append("SuperClassReferencePattern: <"); //$NON-NLS-1$

	if (superSimpleName != null)
		output.append(superSimpleName);
	else
		output.append("*"); //$NON-NLS-1$
	output.append(">"); //$NON-NLS-1$
	return super.print(output);
}
}
