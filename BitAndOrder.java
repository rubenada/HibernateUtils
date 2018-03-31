package org.rubenada.hibernate.utils;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

import java.sql.Types;

/**
 * Represents an ordering imposed upon the results of a Criteria, with a bitwise AND operation, i.e.
 *     ORDER BY (property & value)  asc | desc
 *
 * WARNING: Tested only with PostgreSQL
 */
public class BitAndOrder extends Order {
    private int value;

    protected BitAndOrder(boolean ascending, String property, int value) {
        super(property, ascending);
        this.value = value;
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
        final String[] columns = criteriaQuery.getColumnsUsingProjection( criteria, getPropertyName() );
        final Type type = criteriaQuery.getTypeUsingProjection( criteria, getPropertyName() );
        final SessionFactoryImplementor factory = criteriaQuery.getFactory();

        final StringBuilder fragment = new StringBuilder();
        for ( int i=0; i<columns.length; i++ ) {
            final StringBuilder expression = new StringBuilder();
            expression.append( '(' );
            expression.append( columns[i] );
            expression.append( " & " );
            expression.append( value );
            expression.append( ')' );

            fragment.append(
                    factory.getDialect().renderOrderByElement(
                            expression.toString(),
                            null,
                            isAscending() ? "asc" : "desc",
                            factory.getSettings().getDefaultNullPrecedence()
                    )
            );
            if ( i < columns.length-1 ) {
                fragment.append( ", " );
            }
        }

        return fragment.toString();
    }

    @Override
    public String toString() {
        return value + " & " + super.toString();
    }

    public static Order asc(String property, int value) {
        return new BitAndOrder(true, property, value);
    }

    public static Order desc(String property, int value) {
        return new BitAndOrder(false, property, value);
    }
}
