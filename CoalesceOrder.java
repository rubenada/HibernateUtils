package org.rubenada.hibernate.utils;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

import java.sql.Types;
import java.util.Arrays;

/**
 * Represents an ordering imposed upon the results of a Criteria, with a coalesce expression, i.e.
 *     ORDER BY COALESCE (property1, property2, ...)  asc | desc
 *
 * WARNING: Tested only with PostgreSQL
 */
public class CoalesceOrder extends Order {
    private String[] properties;

    protected CoalesceOrder(boolean ascending, String... properties) {
        super(properties.toString(), ascending);
        this.properties = properties;
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {

        StringBuilder fragment = new StringBuilder();
        StringBuilder expression = new StringBuilder();
        fragment.append("COALESCE(");
        SessionFactoryImplementor factory = criteriaQuery.getFactory();

        for (int j = 0; j < this.properties.length; j++) {
            String propertyName = this.properties[j];
            String[] columns = criteriaQuery.getColumnsUsingProjection(
                    criteria, propertyName);
            Type type = criteriaQuery.getTypeUsingProjection(criteria,
                    propertyName);
            StringBuilder fragmentForField = new StringBuilder();
            for (int i = 0; i < columns.length; i++) {
                StringBuilder expressionForField = new StringBuilder();
                boolean lower = false;
                if (isIgnoreCase()) {
                    int sqlType = type.sqlTypes(factory)[i];
                    lower = sqlType == Types.VARCHAR
                            || sqlType == Types.CHAR
                            || sqlType == Types.LONGVARCHAR;
                }

                if (lower) {
                    expressionForField.append(
                            factory.getDialect().getLowercaseFunction())
                            .append('(');
                }
                expressionForField.append(columns[i]);
                if (lower)
                    expressionForField.append(')');
                fragmentForField.append(expressionForField.toString());
                if (i < columns.length - 1)
                    fragmentForField.append(", ");
            }
            expression.append(fragmentForField.toString());
            if (j < properties.length - 1)
                expression.append(", ");
        }

        expression.append(")");

        fragment.append(factory.getDialect().renderOrderByElement(
                expression.toString(), null, isAscending() ? "asc" : "desc",
                factory.getSettings().getDefaultNullPrecedence()));
        return fragment.toString();
    }

    @Override
    public String toString() {
        return "COALESCE "
                + Arrays.toString(properties)
                + ( isAscending() ? " asc" : " desc" );
    }

    public static Order asc(String... properties) {
        return new CoalesceOrder(true, properties);
    }

    public static Order desc(String... properties) {
        return new CoalesceOrder(false, properties);
    }
}
