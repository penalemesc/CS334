class atset(frozenset):
    '''A subclass of frozenset intended for storing a set of attributes.'''
    def __str__(self) -> str:
        result = ""
        for item in iter(self):
            result += str(item)
        result += ""
        return result


class fd:
    '''Class for storing a functional dependency, where the LHS and RHS are
    intendeded to each by atsets. This class is hashable, so it can be stored
    in a set of fds.
    '''
    def __init__(self, LHS: atset, RHS: atset):
        self.LHS = LHS
        self.RHS = RHS

    def __hash__(self) -> int:
        return hash(tuple((self.LHS, self.RHS)))

    def __eq__(self, other) -> bool:
        return self.LHS == other.LHS and self.RHS == other.RHS

    def __str__(self) -> str:
        return str(self.LHS) + " -> " + str(self.RHS)


class fdset(set):
    '''A subclass of set intended for storing a set of functional
    dependencies.'''
    def __str__(self) -> str:
        result = ""
        for item in iter(self):
            result += str(item) + "\n"
        return result


class relnset(set):
    '''A subclass of set intended for storing a set of relations. Relations are
    only just atsets, but they display differently when printing.'''
    def __str__(self) -> str:
        result = "{"
        for item in iter(self):
            result += str(item) + " "
        result += "}"
        return result

# -> significa que eso devuelve la funcion
def closure(attrs: atset, fds: fdset) -> fdset:
    '''Given a set of attributes (atset) and a set of functional dependencies
    (fdset), produce a new set of functional dependencies (fdset) that
    represents the closure of the original fdset.'''
    """
    apply the reflexivity rule
    repteat
        for each f in F+
        for each  pair of func dependencies if1 and f2 in f+
            if f1 and f2 can be combined using transitivity, add it to f+
    until f+ does not change further
    """

    "transitiva: if a -> b holds and b -> y holds, then a -> y holds "

	#this is only for the transitive axiom, I do not know how to properly do the other axioms
    fds2 = fds.copy()
    for i in iter(fds):
        for j in iter(fds):
            #Buena practica poner a la izquierda lo que esta en el for de arriba
            
            #This adds every fd that it finds, including those that were already there,
            #I assume there is a way to check if a fd however I dont really know how to
            if (i.RHS == j.LHS or i.LHS == j.RHS) & (i.RHS == j.LHS or i.LHS == j.RHS):
                   fds2.add(fd(atset([i.LHS]), atset([j.RHS])))
    
            """if (i.LHS == j.LHS or i.LHS == j.RHS) & (i.RHS == j.LHS or i.LHS == j.RHS):
                fds2.add(fd(atset([i.LHS]), atset([j.LHS])))"""   
            #este test era para ver que hacia el meetodo eq que esta en la definicion de fd
            #if (i.__eq__(j)):
                #fds2.add(fd(atset([i]), atset([j])))
            #else: 
                #print("test2 ",i, j)
                #fds.add(fd(atset([]), atset([])))
            #print(i.LHS, j.LHS, i.RHS, j.RHS)
    fds = fds2
    return fdset(fds)


def bcnf(reln, fds) -> relnset:
    '''Given a relation represented as set of attributes (atset) and a set of
    functional dependencies (fdset), produce a set of relations that represents
    the BCNF decomposition of the original relation.'''
    fds2 = fds.copy()
    #I dont fully know how to do bcnf in python, but the way i would go about it is by creating a candidate 
    #key finder method, then for every super key in the relation, create a new relation where that 
    #candidate key is a super key for the attributes that are dependant on it so that if the atributes look like:
    #X -> Y X is a super key for that relation
    """def atrIsCandidateKey():
        for i in reln:
            if 
    def isBCNF(fds, attrs):
        for i in fds:"""
            
    result = relnset()
    result.add(reln)
    return result


def main() -> None:
    attributes = atset([1, 2, 3])
    print("Some attributes:", attributes)

    fds = fdset()
    fds.add(fd(atset([1]), atset([2])))
    fds.add(fd(atset([2]), atset([1])))
    fds.add(fd(atset([2]), atset([3])))
    print("Some fds:")
    print(fds)

    result = closure(attributes, fds)
    print("A closure:")
    print(result)

    decomp_result = bcnf(attributes, fds)
    print("A decomposition:", decomp_result)


if __name__ == "__main__":
    main()
