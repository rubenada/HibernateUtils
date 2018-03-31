package org.rubenada.hibernate.utils;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.type.IntegerType;


/**
 * Criterion for bitwise AND operation
 * WARNING: Tested only with PostgreSQL
 */
public class BitAnd implements Criterion {
    private final String propertyName;
    private final int value;
    private final String comparisonOperator;
    private final int comparisonValue;

    /**
     * Creates a bitwise AND criterion that will be evaluated as:
     *     (propertyName & value) comparisonOperator comparisonValue
     * e.g.:
     *     (myProperty & 256) = 0
     *
     * @param propertyName property name (first operator of the AND)
     * @param value integer value (second operator of the AND)
     * @param comparisonOperator comparison operator
     * @param comparisonValue integer value to be compared to the AND result
     */
    public BitAnd(final String propertyName, final int value,
                  final String comparisonOperator, final int comparisonValue) {
        this.propertyName = propertyName;
        this.value = value;
        this.comparisonOperator = comparisonOperator;
        this.comparisonValue = comparisonValue;
    }

    /**
     * @param propertyName property name (first operator of the AND)
     * @param value integer value (second operator of the AND)
     * @param comparisonValue integer value to be compared to the AND result
     * @return bitwise AND criterion with the form: (propertyName & value) = comparisonValue
     */
    public static BitAnd eq (final String propertyName, final int value, final int comparisonValue) {
        return new BitAnd (propertyName, value, "=", comparisonValue);
    }


    /**
     * @param propertyName property name (first operator of the AND)
     * @param value integer value (second operator of the AND)
     * @param comparisonValue integer value to be compared to the AND result
     * @return bitwise AND criterion with the form: (propertyName & value) != comparisonValue
     */
    public static BitAnd ne (final String propertyName, final int value, final int comparisonValue) {
        return new BitAnd (propertyName, value, "<>", comparisonValue);
    }

    @Override
    public String toSqlString(final Criteria criteria,
                              final CriteriaQuery criteriaQuery) throws HibernateException {
        final String[] columns = criteriaQuery.getColumnsUsingProjection(
                criteria, this.propertyName);
        final String queryFragment = "( " + columns[0] + " & ? ) " + this.comparisonOperator + " ?";
        return queryFragment;
    }

    @Override
    public TypedValue[] getTypedValues(final Criteria criteria,
                                       final CriteriaQuery criteriaQuery) throws HibernateException {
        return new TypedValue[] { new TypedValue(IntegerType.INSTANCE, Integer.valueOf(value)),
                new TypedValue(IntegerType.INSTANCE, Integer.valueOf(comparisonValue))};
    }
}
