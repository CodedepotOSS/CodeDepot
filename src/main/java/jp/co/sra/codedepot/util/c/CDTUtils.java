/**
* Copyright (c) 2009 SRA (Software Research Associates, Inc.)
*
* This file is part of CodeDepot.
* CodeDepot is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3.0
* as published by the Free Software Foundation and appearing in
* the file GPL.txt included in the packaging of this file.
*
* CodeDepot is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
*
**/
package jp.co.sra.codedepot.util.c;

import java.util.ArrayList;
import java.util.logging.Logger;

import jp.co.sra.codedepot.search.SignatureQuery;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;

/**
 * A utility class for CDT.
 * $Id: CDTUtils.java 2356 2017-11-10 07:50:30Z fang $
 */
public class CDTUtils {

    private static Logger logger = Logger.getLogger(CDTUtils.class.getName());

    /**
     * Get a start line number in file of a given AST node.
     *
     * @return
     *	A line number. If the node is not well-shaped, returns -1.
     *
     * @param node
     *	An AST node.
     */
    public static int getStartLine(IASTNode node) {
        IASTFileLocation fl = node.getFileLocation();
        if (fl == null) {
            return -1;
        }
        return fl.getStartingLineNumber();
    }


    /**
     * Get a end line number in file of a given AST node.
     *
     * @return
     *	A line number. If the node is not well-shaped, returns -1.
     *
     * @param node
     *	An AST node.
     */
    public static int getEndLine(IASTNode node) {
        IASTFileLocation fl = node.getFileLocation();
        if (fl == null) {
            return -1;
        }
        return fl.getEndingLineNumber();
    }


    /**
     * Get a filename of a file that a given AST exists.
     *
     * @return
     *	A filename.
     *
     * @param node
     *	An AST node.
     */
    public static String getFilename(IASTNode node) {

    	// ASTメソッド内でのNullPointerExceptionの発生を回避するための暫定対策
		if (null == node.getFileLocation()) {
			return null;
		}

        String ret = node.getContainingFilename();
        /*
         * FIXME:
         *	Since we just don't use CodeReader the filename in it
         *	is not initialized. So if we got "<text>", means that
         *	is uninitialized filename, returns null.
         */
        if (ret.equals("<text>") == true) {
            ret = null;
        }
        return ret;
    }


    /**
     * Get an identifier name for a given AST node, without any
     * scope names.
     *
     * @return
     *	A name of the identifier.
     *
     * @param node
     *	An AST name.
     *
     * @param expandKind
     *	If true, a kind of the identifier is added to the returned
     *	string.
     */
    public static String getScopelessName(IASTName node,
                                          boolean expandKind) {
        String ret = null;
        String termName = null;
        String fullName = node.toString();
        int rClnIdx = fullName.lastIndexOf("::");

        if (rClnIdx < 0) {
            termName = fullName;
        } else {
            termName = fullName.substring(rClnIdx + 2);
        }

        if (expandKind == true) {
            DeclarationKinds k = getKind(node);
            ret = k.toString();
            ret += " ";
            ret += termName;
        } else {
            ret = termName;
        }

        return ret;
    }


    /**
     * Get an identifier name for a given AST node, without any
     * scope names.
     *
     * @return
     *	A name of an identifier.
     *
     * @param node
     *	An AST name.
     */
    public static String getScopelessName(IASTName node) {
        return getScopelessName(node, false);
    }


    private static IScope getUniqueScopeNode_BOGUS(IASTName node) {
        /*
         * The CDT seems having some bugs around scope name handling,
         * in which it causes identical namespace successively be
         * nested like: "NS0::C0::C0::func" in IScope link for
         * parameters/variables.
         *
         * I don't have any ideas avoiding this in logically, So
         * taking this ad-hoc method.
         */
        IBinding b = node.resolveBinding();
        if (b == null) {
            return null;
        }
        if ((b instanceof IProblemBinding) == true) {
            /*
            IProblemBinding pb = (IProblemBinding)b;
            System.err.printf("%s: line %5d: '%s'\n",
                              pb.getFileName(),
                              pb.getLineNumber(),
                              pb.getMessage());
            */
            return null;
        }

        String nodeName = getScopelessName(node);
        if (nodeName == null || nodeName.length() <= 0) {
            return null;
        }

        try {
            /*
             * Firstly, get a current scope's name.
             */
            IScope baseScope = b.getScope();
            if (baseScope == null) {
                return null;
            }
            IName baseIN = baseScope.getScopeName();
            if (baseIN == null) {
                return null;
            }
            String baseName = getScopelessName((IASTName)baseIN);
            if (baseName == null || baseName.length() <= 0) {
                return null;
            }

            /*
             * Then get a parent scope's name.
             */
            IScope parentScope = baseScope.getParent();
            if (parentScope == null) {
                /*
                 * The baseNode is the top scope. Returns baseScope.
                 */
                return baseScope;
            }
            IName parentIN = parentScope.getScopeName();
            if (parentIN == null) {
                /*
                 * Same above, returns baseScope.
                 */
                return baseScope;
            }
            String parentName = getScopelessName((IASTName)parentIN);
            if (parentName == null || parentName.length() <= 0) {
                /*
                 * And again, returns baseScope.
                 */
                return baseScope;
            }

/*
            System.err.printf("\t\tnode '%s', base '%s', parent '%s'\n",
                              nodeName, baseName, parentName);
*/

            /*
             * Finally, compare those three names.
             */
            if (baseName.equals(parentName) == true ||
                baseName.equals(nodeName) == true) {
                /*
                 * Returns parentScope.
                 */
                return parentScope;
            } else {
                return baseScope;
            }
        } catch (Exception e) {
	    logger.fine("CDT: " + e.toString());
            return null;
        }
    }


    private static boolean isIdenticalScope(IScope s0, IScope s1) {
        if (s0 == null && s1 == null) {
            return true;
        } else if (s0 == null || s1 == null) {
            return false;
        }

        //System.err.printf("isID0\n");

        IASTName s0Name = null;
        IASTName s1Name = null;
        try {
            s0Name = (IASTName)(s0.getScopeName());
            s1Name = (IASTName)(s1.getScopeName());
        } catch (Exception e) {
	    logger.fine("CDT: " + e.toString());
        }

        if (s0Name == null && s1Name == null) {
            /*
             * Has no name. Treat its as same one.
             */
            return true;
        } else if (s0Name == null || s1Name == null) {
            return false;
        }

        //System.err.printf("isID1\n");

        DeclarationKinds s0k = getKind(s0Name);
        DeclarationKinds s1k = getKind(s1Name);

        if (s0k != s1k) {
            return false;
        }

        //System.err.printf("isID2\n");

        String s0Str = s0Name.toString();
        String s1Str = s1Name.toString();
        if ((s0Str == null || s0Str.length() <= 0) &&
            (s1Str == null || s1Str.length() <= 0)) {
            return true;
        } else if ((s0Str == null || s0Str.length() <= 0) ||
                   (s1Str == null || s1Str.length() <= 0)) {
            return false;
        }
        if (s0Str.equals(s1Str) == false) {
            return false;
        }

        //System.err.printf("isID3\n");

        IBinding s0BInS1 = null;
        IBinding s1BInS0 = null;
        try {
            s0BInS1 = s0.getBinding(s1Name, true);
            s1BInS0 = s1.getBinding(s0Name, true);
        } catch (Exception e) {
	    logger.fine("CDT: " + e.toString());
        }

/*
        String inS1Str = null;
        if (s0BInS1 != null) {
            inS1Str = s0BInS1.getClass().toString();
        }
        String inS0Str = null;
        if (s1BInS0 != null) {
            inS0Str = s1BInS0.getClass().toString();
        }

        System.err.printf("\t\ts0 '%s', s1 '%s'\n", inS1Str, inS0Str);
*/

        if (s0BInS1 == null && s1BInS0 == null) {
            /*
             * SUPER BOGUS
             *	FIXME:
             *		Very wrong.
             */
            return true;
        } else if (s0BInS1 == null || s1BInS0 == null) {
            return false;
        } else if (s0BInS1 != null && s1BInS0 != null) {
            return true;
        }

        return false;
    }


    private static boolean isIdenticalScope(IASTName s0Node,
                                            IASTName s1Node) {
        if (s0Node == null || s1Node == null) {
            return false;
        }
        IBinding b0 = s0Node.resolveBinding();
        IBinding b1 = s1Node.resolveBinding();
        if (b0 == null || b1 == null) {
            return false;
        }

        IScope s0 = null;
        IScope s1 = null;
        try {
            s0 = b0.getScope();
            s1 = b1.getScope();
        } catch (Exception e) {
	    logger.fine("CDT: " + e.toString());
        }

        return isIdenticalScope(s0, s1);
    }


    private static boolean hasSaneScope(IASTName node) {
        IBinding b = node.resolveBinding();
        if (b == null) {
            return false;
        }
        if ((b instanceof IProblemBinding) == true) {
            /*
            IProblemBinding pb = (IProblemBinding)b;
            System.err.printf("%s: line %5d: '%s'\n",
                              pb.getFileName(),
                              pb.getLineNumber(),
                              pb.getMessage());
            */
            return false;
        }

        try {
            IScope s = b.getScope();
            if (s == null) {
                return false;
            }

            IScope ps = s.getParent();
            if (ps == null) {
                return true;
            }

            if (isIdenticalScope(s, ps) == true) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }


    private static IScope getUniqueScopeNode(IASTName node) {
        if (hasSaneScope(node) == false) {
            //System.err.printf("\t%s: scope insane.\n", node.toString());
            return null;
        }

        IBinding b = node.resolveBinding();
        if (b == null) {
            return null;
        }
        if ((b instanceof IProblemBinding) == true) {
            /*
            IProblemBinding pb = (IProblemBinding)b;
            System.err.printf("%s: line %5d: '%s'\n",
                              pb.getFileName(),
                              pb.getLineNumber(),
                              pb.getMessage());
            */
            return null;
        }

        IScope retScope = null;
        try {
            IScope s = b.getScope();
            if (s != null) {
                IName iName = s.getScopeName();
                if (iName != null) {
                    String nStr = getScopelessName((IASTName)iName);
                    if (nStr != null && nStr.length() > 0) {
                        retScope = s;
                    }
                }
            }
        } catch (Exception e) {
	    logger.fine("CDT: " + e.toString());
        }

        return retScope;
    }


    /**
     * Get a scope of a given AST node.
     *
     * @return
     *	A scope of the identifier. (Could be null.)
     *
     * @param node
     *	An AST name.
     */
    public static IScope getScope(IASTName node) {
        return getUniqueScopeNode(node);
    }


    /**
     * Get a parent scope of a given AST node.
     *
     * @return
     *	A parent scope of the identifier. (Could be null.)
     *
     * @param node
     *	An AST name.
     */
    public static IScope getParentScope(IASTName node) {
        IScope myS = getScope(node);
        if (myS == null) {
            return null;
        }

        IScope ret = null;
        try {
            ret = myS.getParent();
        } catch (Exception e) {
            /*
             * Do nothing.
             */
        }

        return ret;
    }


    /**
     * Get an AST node of a given scope.
     *
     * @return
     *	An AST node of the scope. (Could be null.)
     *
     * @param node
     *	A scope.
     */
    public static IASTName getScopeNode(IScope s) {
        if (s == null) {
            return null;
        }

        IName ret = null;
        try {
            ret = s.getScopeName();
        } catch (Exception e) {
            /*
             * Do nothing.
             */
        }

        return (IASTName)ret;
    }


    /**
     * Get an AST node of a given node's scope.
     *
     * @return
     *	An AST node of the scope. (Could be null.)
     *
     * @param node
     *	An AST node.
     */
    public static IASTName getScopeNode(IASTName node) {
        IScope s = getScope(node);
        if (s == null) {
            return null;
        }

        IName ret = null;
        try {
            ret = s.getScopeName();
        } catch (Exception e) {
            /*
             * Do nothing.
             */
        }

        return (IASTName)ret;
    }


    /**
     * Get a parant AST node of a given scope.
     *
     * @return
     *	A parent AST node of the scope. (Could be null.)
     *
     * @param node
     *	A scope.
     */
    public static IASTName getParentScopeNode(IScope s) {
        if (s == null) {
            return null;
        }

        IName ret = null;
        try {
            s = s.getParent();
            if (s == null) {
                return null;
            }
            ret = s.getScopeName();
        } catch (Exception e) {
            /*
             * Do nothing.
             */
        }

        return (IASTName)ret;
    }


    /**
     * Get a parant AST node of a given node's scope.
     *
     * @return
     *	A parent AST node of the scope. (Could be null.)
     *
     * @param node
     *	An AST node.
     */
    public static IASTName getParentScopeNode(IASTName node) {
        IScope s = getParentScope(node);
        if (s == null) {
            return null;
        }

        IName ret = null;
        try {
            ret = s.getScopeName();
        } catch (Exception e) {
            /*
             * Do nothing.
             */
        }

        return (IASTName)ret;
    }


    /**
     * Get a scope name of a given scope.
     *
     * @return
     *	A scope name of the scope. (Could be null.)
     *
     * @param node
     *	A scope.
     */
    public static String getScopeName(IScope s) {
        if (s == null) {
            return null;
        }

        IASTName n = getScopeNode(s);
        if (n == null) {
            return null;
        }

        String ret = n.toString();
        if (ret == null || ret.length() <= 0) {
            return null;
        }
        return ret;
    }


    /**
     * Get a parent scope name of a given scope.
     *
     * @return
     *	A parent scope name of the scope. (Could be null.)
     *
     * @param node
     *	A scope.
     */
    public static String getParentScopeName(IScope s) {
        if (s == null) {
            return null;
        }
        try {
            s = s.getParent();
        } catch (Exception e) {
            /*
             * Do nothing.
             */
        }

        return getScopeName(s);
    }


    /**
     * Get a scope name of a given AST node.
     *
     * @return
     *	A scope name of the given identifier. (Could be null.)
     *
     * @param node
     *	An AST name.
     */
    public static String getScopeName(IASTName node) {
        IScope s = getScope(node);
        if (s == null) {
            return null;
        }

        return getScopeName(s);
    }


    /**
     * Get a parent scope name of a given AST node.
     *
     * @return
     *	A parent scope name of the given identifier. (Could be null.)
     *
     * @param node
     *	An AST name.
     */
    public static String getParentScopeName(IASTName node) {
        IScope s = getParentScope(node);
        if (s == null) {
            return null;
        }

        return getScopeName(s);
    }


    /**
     * Get a scope kind of a given scope.
     *
     * @return
     *	A kind of the given scope. (Could be null.)
     *
     * @param s
     *	A scope.
     */
    public static DeclarationKinds getScopeKind(IScope s) {
        IASTName n = getScopeNode(s);
        return getKind(n);
    }


    /**
     * Get a scope kind of a given AST node.
     *
     * @return
     *	A kind of the given scope. (Could be null.)
     *
     * @param node
     *	An AST node.
     */
    public static DeclarationKinds getScopeKind(IASTName node) {
        IASTName n = getScopeNode(node);
        return getKind(n);
    }


    /**
     * Get a scope kind of a given scope.
     *
     * @return
     *	A kind of the given scope. (Could be null.)
     *
     * @param s
     *	A scope.
     */
    public static DeclarationKinds getParentScopeKind(IScope s) {
        IASTName n = getParentScopeNode(s);
        return getKind(n);
    }


    /**
     * Get a parent scope kind of a given AST node.
     *
     * @return
     *	A parent scope kind of the given scope. (Could be null.)
     *
     * @param node
     *	An AST node.
     */
    public static DeclarationKinds getParentScopeKind(IASTName node) {
        IASTName n = getParentScopeNode(node);
        return getKind(n);
    }


    /**
     * Get an identifier name for the given AST node, with fully
     * resolved absolute scope names.
     *
     * @return
     *	An absolute scope name.
     *
     * @param node
     *	An AST name.
     *
     * @param includeSelf
     *	If true, a name of the identifier is added to the returned
     *	absolute scope name.
     *
     * @param expandKind
     *	If true, a kind of the identifier is added to the returned
     *	absolute scope name.
     */
    public static String getAbsoluteScopeName(IASTName node,
                                              boolean includeSelf,
                                              boolean expandKind) {
        ArrayList<String> scList = new ArrayList<String>();
        String sTmp = null;
        IASTName name = null;
        IASTName nextName = null;

/*
        System.err.printf("\tnode: '%s'\n",
                          node.toString());
*/

        name = getScopeNode(node);
        while (name != null) {

            //System.err.printf("\t\tname: '%s'\n", name.toString());

            sTmp = getScopelessName(name, expandKind);

            if (sTmp != null && sTmp.length() > 0) {
                if (expandKind == true) {
                    DeclarationKinds k = getKind(name);
                    String sTmp2 = k.toString();
                    sTmp2 += " ";
                    sTmp2 += sTmp;
                    scList.add(0, sTmp2);
                } else {
                    scList.add(0, sTmp);
                }
            } else {
                break;
            }


            nextName = getScopeNode(name);
            if (isIdenticalScope(name, nextName) == true) {
                nextName = getParentScopeNode(name);
            }
            if (nextName == null) {
                break;
            }
            name = nextName;
        }

        String ret = null;

        int n = scList.size();
        if (n > 0) {
            String[] retA =
                (String[])scList.toArray(new String[scList.size()]);
            ret = new String("");
            ret += retA[0];
            for (int i = 1; i < n; i++) {
                ret += "::";
                ret += retA[i];
            }

            //System.err.printf("\t\t'%s'\n", ret);

            if (includeSelf == true) {
                ret += "::";
                ret += getScopelessName(node, expandKind);
            }
        } else {
            if (includeSelf == true) {
                ret = getScopelessName(node, expandKind);
            }
        }

        return ret;
    }


    /**
     * Get an identifier name for the given AST node, with fully
     * resolved absolute scope names.
     *
     * @return
     *	An absolute scope name.
     *
     * @param node
     *	An AST name.
     *
     * @param includeSelf
     *	if true, a name of the identifier is added to the returned
     *	absolute scope name.
     */
    public static String getAbsoluteScopeName(IASTName node,
                                              boolean includeSelf) {
        return getAbsoluteScopeName(node, includeSelf, false);
    }


    /**
     * Guess a kind of a given node.
     *
     * @return
     *	A kind of the node.
     *
     * @param node
     *	An AST name.
     */
    public static DeclarationKinds guessKind(IASTName node) {
        DeclarationKinds ret = DeclarationKinds.KIND_UNKNOWN;
        if (node == null) {
            return ret;
        }

        IBinding b = node.resolveBinding();

        if (b != null) {
            return getKind(node);
        }

        IASTNode p = node.getParent();
        if (p == null) {
            return ret;
        }

        /*
         * Guessing the kind.
         */
        if ((p instanceof IASTDeclarator) == true) {
            IASTNode pp = p.getParent();

            if ((pp instanceof IASTParameterDeclaration) == true) {
                /*
                 * A function prototype, thus the node should be a
                 * parameter.
                 */
                ret = DeclarationKinds.KIND_PARAMETER;
            }
            /*
             * FIXME:
             *	Add other cases below.
             */
        }

        return ret;
    }


    /**
     * Get a kind of a given node.
     *
     * @return
     *	A kind of the node.
     *
     * @param node
     *	An AST name.
     */
    public static DeclarationKinds getKind(IASTName node) {
        DeclarationKinds ret = DeclarationKinds.KIND_UNKNOWN;
        if (node == null) {
            return ret;
        }

        IBinding b = node.resolveBinding();

        if (b == null) {
            return guessKind(node);
        }

        if ((b instanceof ICPPTemplateTypeParameter) == true) {
            ret = DeclarationKinds.KIND_TEMPLATETYPE;
        } else if ((b instanceof ICPPNamespace) == true) {
            ret = DeclarationKinds.KIND_NAMESPACE;
        } else if ((b instanceof ICPPUsingDeclaration) == true) {
            ret = DeclarationKinds.KIND_USING;
        } else if ((b instanceof ITypedef) == true) {
            if ((b instanceof IParameter) == true) {
                ret = DeclarationKinds.KIND_PARAMETER;
            } else {
                ret = DeclarationKinds.KIND_NAMED;
            }
        } else if ((b instanceof ICompositeType) == true) {
            int k = Integer.MAX_VALUE;
            try {
                k = ((ICompositeType)b).getKey();
            } catch (Exception e) {
	    	logger.fine("CDT: " + e.toString());
                return ret;
            }
            switch (k) {
                case ICompositeType.k_struct: {
                    ret = DeclarationKinds.KIND_STRUCT;
                    break;
                }
                case ICompositeType.k_union: {
                    ret = DeclarationKinds.KIND_UNION;
                    break;
                }
                case ICPPClassType.k_class: {
                    ret = DeclarationKinds.KIND_CLASS;
                    break;
                }
            }
        } else if ((b instanceof IEnumeration) == true) {
            ret = DeclarationKinds.KIND_ENUM;
        } else if ((b instanceof IEnumerator) == true) {
            /*
             * Treat as a member.
             */
            ret = DeclarationKinds.KIND_MEMBER;
        } else if ((b instanceof IField) == true) {
            ret = DeclarationKinds.KIND_MEMBER;
        } else if ((b instanceof IFunction) == true) {
            ret = DeclarationKinds.KIND_FUNCTION;
        } else if ((b instanceof IParameter) == true) {
            ret = DeclarationKinds.KIND_PARAMETER;
        } else if ((b instanceof IVariable) == true) {
            ret = DeclarationKinds.KIND_VARIABLE;
        } else if ((b instanceof ICPPClassType) == true) {
            ret = DeclarationKinds.KIND_CLASS;
        } else if ((b instanceof ILabel) == true) {
            ret = DeclarationKinds.KIND_LABEL;
        } else if ((b instanceof IProblemBinding) == true) {
            /*
             * return KIND_UNKNOWN
             */
        } else {
            System.err.printf(
                "\n\t\tundetermind kind from binding: " +
                "name: '%s': bind: '%s'\n\n",
                node.toString(),
                b.getClass().toString());
        }

        return ret;
    }


    /**
     * Get a kind of a given node.
     *
     * @return
     *	A kind of the node.
     *
     * @param dNode
     *	An AST declarator node.
     */
    public static DeclarationKinds getKind(IASTDeclarator dNode) {
        IASTName dName = dNode.getName();
        return getKind(dName);
    }


    /**
     * Get a kind of a given node.
     *
     * @return
     *	A kind of the node.
     *
     * @param sDecl
     *	An AST declaration specifier node.
     */
    public static DeclarationKinds getKind(IASTDeclSpecifier sDecl) {

        DeclarationKinds ret = DeclarationKinds.KIND_UNKNOWN;

        if ((sDecl instanceof IASTCompositeTypeSpecifier) == true) {

            /*
             * struct/union/class.
             */
            IASTCompositeTypeSpecifier cts = (IASTCompositeTypeSpecifier)sDecl;
            switch (cts.getKey()) {
                case IASTCompositeTypeSpecifier.k_struct: {
                    ret = DeclarationKinds.KIND_STRUCT;
                    break;
                }
                case IASTCompositeTypeSpecifier.k_union: {
                    ret = DeclarationKinds.KIND_UNION;
                    break;
                }
                case ICPPASTCompositeTypeSpecifier.k_class: {
                    ret = DeclarationKinds.KIND_CLASS;
                    break;
                }
                default: {
                    break;
                }
            }

        } else if ((sDecl instanceof IASTElaboratedTypeSpecifier) == true) {

            /*
             * struct/union/enum/class.
             */
            IASTElaboratedTypeSpecifier ets =
                (IASTElaboratedTypeSpecifier)sDecl;
            switch (ets.getKind()) {
                case IASTElaboratedTypeSpecifier.k_struct: {
                    ret = DeclarationKinds.KIND_STRUCT;
                    break;
                }
                case IASTElaboratedTypeSpecifier.k_union: {
                    ret = DeclarationKinds.KIND_UNION;
                    break;
                }
                case IASTElaboratedTypeSpecifier.k_enum: {
                    ret = DeclarationKinds.KIND_ENUM;
                    break;
                }
                case ICPPASTElaboratedTypeSpecifier.k_class: {
                    ret = DeclarationKinds.KIND_CLASS;
                    break;
                }
                default: {
                    break;
                }
            }

        } else if ((sDecl instanceof IASTEnumerationSpecifier) == true) {

            /*
             * enum.
             */
            ret = DeclarationKinds.KIND_ENUM;

        } else if ((sDecl instanceof IASTNamedTypeSpecifier) == true) {

            /*
             * typedef.
             */
            ret = DeclarationKinds.KIND_NAMED;

        } else if ((sDecl instanceof IASTSimpleDeclSpecifier) == true) {

            /*
             * basic type.
             */
             ret = DeclarationKinds.KIND_BASICTYPE;

        } else {
            System.err.printf("\t\tundetermind spec kind from ASTNode: " +
                              "'%s'\n\n",
                              sDecl.getClass().toString());
        }

        return ret;
    }


    /**
     * Get an AST name of a given node.
     *
     * @return
     *	An AST name.
     *
     * @param sDecl
     *	An AST declaration specifier node.
     */
    public static IASTName getName(IASTDeclSpecifier sDecl) {
        IASTName ret = null;

        if ((sDecl instanceof IASTCompositeTypeSpecifier) == true) {

            /*
             * struct/union/class.
             */
            ret = ((IASTCompositeTypeSpecifier)sDecl).getName();

        } else if ((sDecl instanceof IASTElaboratedTypeSpecifier) == true) {

            /*
             * struct/union/enum/class.
             */

            ret = ((IASTElaboratedTypeSpecifier)sDecl).getName();

        } else if ((sDecl instanceof IASTEnumerationSpecifier) == true) {

            /*
             * enum.
             */
            ret = ((IASTEnumerationSpecifier)sDecl).getName();

        } else if ((sDecl instanceof IASTNamedTypeSpecifier) == true) {

            /*
             * typedef.
             */
            ret = ((IASTNamedTypeSpecifier)sDecl).getName();

        } else if ((sDecl instanceof IASTSimpleDeclSpecifier) == true) {

            /*
             * basic type.
             */
            ret = null;

        } else {
            System.err.printf("\t\tunsupported ASTNode: " +
                              "'%s'\n\n",
                              sDecl.getClass().toString());
        }

        return ret;
    }


    /**
     * Get a function signature of a given node.
     *
     * @return
     *	A signature of the function node.
     *
     * @param node
     *	An AST name.
     */
    public static String getFunctionSignature(IASTName node) {
        String ret = null;

        if (getKind(node) != DeclarationKinds.KIND_FUNCTION) {
            return null;
        }

        IBinding b = node.resolveBinding();
        if (b == null) {
            return null;
        }

        if ((b instanceof IFunction) == true) {
            IFunctionType ft = null;
            IType retType = null;
            IType[] paramTypes = null;

            try {
                ft = ((IFunction)b).getType();
                retType = ft.getReturnType();
                paramTypes = ft.getParameterTypes();
            } catch (Exception e) {
	    	logger.fine("CDT: " + e.toString());
                return null;
            }

            String rtStr = ASTTypeUtil.getType(retType, false);
            String paramTStr = new String("(");
            boolean hasParams = false;

            final String paramDelimiter = " " ;

            int n = paramTypes.length;
            if (n > 0) {
                //String firstParam =
                //  ASTTypeUtil.getType(paramTypes[0], false);
                String firstParam = CDTUtils.getType(paramTypes[0],false) ;
                if( firstParam != null ){
                    firstParam = SignatureQuery.replaceSeparator(firstParam);
                }

                if (n == 1 &&
                    firstParam.equals("void") == true) {
                    paramTStr += "void)";
                } else {
                    hasParams = true;
                    paramTStr += firstParam;
                    for (int i = 1; i < n; i++) {
                        //paramTStr += ", ";
                        //paramTStr +=
                        //    ASTTypeUtil.getType(paramTypes[i],
                        //                        false);
                        paramTStr += paramDelimiter ;
                        String param = CDTUtils.getType(paramTypes[i],false) ;
                        if( param != null ){
                            param = SignatureQuery.replaceSeparator(param);
                        }
                        paramTStr += param ;
                    }
                    paramTStr += ")";
                }
            } else {
                paramTStr += ")";
            }

            boolean hasVarArgs = false;
            try {
                hasVarArgs = ((IFunction)b).takesVarArgs();
            } catch (Exception e) {
                /*
                 * Do nothing.
                 */
            }
            if (hasVarArgs == true) {
                if (hasParams == true) {
                    //paramTStr = paramTStr.replace(")", ", ...)");
                    paramTStr = paramTStr.replace(")", paramDelimiter + "...)");
                } else {
                    paramTStr = paramTStr.replace(")", "...)");
                }
            }

            //ret = rtStr + " " +
            ret = CDTUtils.getAbsoluteScopeName(node, true) +
                paramTStr;

        }

        return ret;
    }


    /**
     * Get a semantic kind of a given AST name node.
     *
     * @return
     *	A semantic kind of the node.
     *
     * @param node
     *	An AST name.
     */
    public static INameKinds getSemanticKind(IName node) {
        INameKinds ret = INameKinds.KIND_UNKNOWN;

        if (node.isReference() == true) {
            ret = INameKinds.KIND_REFERENCE;
        } else if (node.isDeclaration() == true) {
            ret = INameKinds.KIND_DECLARATION;
        } else if (node.isDefinition() == true) {
            ret = INameKinds.KIND_DEFINITION;
        } else {
            if ((node instanceof IASTNode) == true) {
                DeclarationKinds k = guessKind((IASTName)node);
                if (k == DeclarationKinds.KIND_PARAMETER) {
                    /*
                     * Should be a declararion.
                     */
                    ret = INameKinds.KIND_DECLARATION;
                }
            }
        }

        return ret;
    }

    /**
     * Returns the type representation of the IType as a String. This function uses the IType interfaces to build the
     * String representation of the IType.
     *
     * @return
     *	The type representation of the IType.
     *
     * @param type
     *	An IType.
     *
     * @param resolveTypedefs
     *	Whether or not typedefs shall be resolved to their real types.
     */
    // TODO: アドホックな修正のため、正しい取得方法が分かったら修正すること
	public static String getType(IType t, boolean resovleTypeResolution) {
		String typeSimpleName = ASTTypeUtil.getType(t, resovleTypeResolution);
		//System.out.print("IType: " + t + " ==> " + typeSimpleName);
		try {
			if (typeSimpleName == null || typeSimpleName.length() == 0) {
				IType [] list = { t };
				typeSimpleName = ASTTypeUtil.getTypeListString(list);
			}

			String prefixName = "";

			if (typeSimpleName == null || typeSimpleName.length() == 0) {
				prefixName = "";
			} else if (typeSimpleName.startsWith("const ")) {
				prefixName = "const "; // + IndexedFunction.SEPARATOR;
			} else if (typeSimpleName.startsWith("volatile ")) {
				prefixName = "volatile "; // + IndexedFunction.SEPARATOR;
			} else if (typeSimpleName.startsWith("restrict ")) {
				prefixName = "restrict "; // + IndexedFunction.SEPARATOR;
			}

			if (prefixName.length() > 0) {
				typeSimpleName = typeSimpleName.substring(prefixName.length()).trim();
			}

			if (typeSimpleName == null || typeSimpleName.length() == 0) {
				if (t instanceof IProblemBinding) {
					IProblemBinding ipb = (IProblemBinding) t;
					typeSimpleName = prefixName + getName(ipb);
				}
			}
			else if (typeSimpleName.startsWith("*") && (t instanceof IPointerType)) {
				IPointerType ipt = (IPointerType) t;
				try {
					IType type = ipt.getType();
					while ((type instanceof IPointerType) || (type instanceof IQualifierType)) {
						if (type instanceof IPointerType) {
							type = ((IPointerType)type).getType();
						}
						if (type instanceof IQualifierType) {
							type = ((IQualifierType)type).getType();
						}
					}
					if (type instanceof IProblemBinding) {
						IProblemBinding ipb = (IProblemBinding) type;
						typeSimpleName = prefixName + getName(ipb)+ SignatureQuery.replaceSeparator(typeSimpleName);
					}
				} catch (DOMException e) {
	    				logger.fine("CDT: " + e.toString());
				}
			}
			else if (typeSimpleName.startsWith("*") && (t instanceof ICPPReferenceType)) {
				ICPPReferenceType irt = (ICPPReferenceType) t;
				try {
					IType type = irt.getType();
					while ((type instanceof IPointerType) || (type instanceof IQualifierType)) {
						if (type instanceof IPointerType) {
							type = ((IPointerType)type).getType();
						}
						if (type instanceof IQualifierType) {
							type = ((IQualifierType)type).getType();
						}
					}
					if (type instanceof IProblemBinding) {
						IProblemBinding ipb = (IProblemBinding) type;
						typeSimpleName = prefixName + getName(ipb)+ SignatureQuery.replaceSeparator(typeSimpleName);
					}
				} catch (DOMException e) {
	    				logger.fine("CDT: " + e.toString());
				}
			}
			else if (typeSimpleName.startsWith("&") && (t instanceof ICPPReferenceType)) {
				ICPPReferenceType irt = (ICPPReferenceType) t;
				try {
					IType type = irt.getType();
					while ((type instanceof IPointerType) || (type instanceof IQualifierType)) {
						if (type instanceof IPointerType) {
							type = ((IPointerType)type).getType();
						}
						if (type instanceof IQualifierType) {
							type = ((IQualifierType)type).getType();
						}
					}
					if (type instanceof IProblemBinding) {
						IProblemBinding ipb = (IProblemBinding) type;
						typeSimpleName = prefixName + getName(ipb)+ SignatureQuery.replaceSeparator(typeSimpleName);
					}
				} catch (DOMException e) {
	    				logger.fine("CDT: " + e.toString());
				}
			} else {
				typeSimpleName = prefixName + typeSimpleName;
			}
		} catch (ClassCastException e) {
	    		logger.fine("CDT: " + e.toString());
		}
		//System.out.println(" ==>" + typeSimpleName);
		return typeSimpleName;
	}

	private static String getName(IProblemBinding ipb) {
		IASTNode nd = ipb.getASTNode();
		if (nd instanceof IASTNamedTypeSpecifier) {
			IASTNamedTypeSpecifier namedTypeNode = (IASTNamedTypeSpecifier) nd;
			CASTName nm = (CASTName) namedTypeNode.getName();
			return nm.toString();
		} else if (nd instanceof IASTName) {
			IASTName nm = (IASTName)nd;
			return new String(nm.toCharArray());
		} else {
			return "";
		}
	}


}
