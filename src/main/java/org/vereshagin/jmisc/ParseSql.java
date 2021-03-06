package org.vereshagin.jmisc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.CCJSqlParserManager;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.Limit;

import net.sf.jsqlparser.JSQLParserException;

import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.expression.Expression;

import java.util.*;
import java.io.*;

/**
 * Parse SQL
 *
 */
public class ParseSql
{
    public static void main( String[] args )
      throws JsonProcessingException, JSQLParserException
    {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable( SerializationFeature.INDENT_OUTPUT );

        String sql;

        sql = "select *, a as c, b from tbl_city, tbl_region";
        printSql( mapper, sql );

        sql = "select *, a as c, b from ( select 1 ) as tbl_city";
        printSql( mapper, sql );

        sql = "select *, a as c, b from tbl_city"
            + " left join tbl_region"
            + " on tbl_region.region_id = tbl_city.region_id"
            + " where tbl_city.id = 63"
        ;
        printSql( mapper, sql );

        sql = "SELECT author.name, count(book.id), sum(book.cost)"
          + " FROM author"
          + " LEFT JOIN book ON (author.id = book.author_id)"
          + " GROUP BY author.name"
          + " HAVING COUNT(*) > 1 AND SUM(book.cost) > 500"
          + " LIMIT 10"
        ;
        printSql( mapper, sql );

        sql = "SELECT *"
          + " FROM products"
          + " LEFT JOIN discounts on products.id = discounts.product_id"
          + " ORDER BY discounts.value, products.price desc"
          + " LIMIT 10, 100"
        ;
        printSql( mapper, sql );

      /*
        if (args.length < 1) {
          System.err.println("Please provide an input!");
          System.exit(0);
        }
        System.out.println(sha256hex(args[0]));
        */

    }


    /*
    public static String sha256hex(String input) {
      return DigestUtils.sha256Hex(input);
    }
    */

    // Print particular sql and its object
    public static void printSql( ObjectMapper mapper, String sql )
      throws JsonProcessingException, JSQLParserException
    {
        SqlQueryObj sqlQueryObj = new SqlQueryObj( sql );

        // Convert object to JSON string
        String sqlQueryJson = mapper.writeValueAsString(sqlQueryObj);

        System.out.println( "SQL: " + sql + "\nJSON: " + sqlQueryJson );

        System.out.println("----");
    }

}

/**
 * Object of SQL Query with its fields
 */
class SqlQueryObj {

  private List<String >  columns     = new ArrayList<String>();
  private List<Source >  fromSources = new ArrayList<Source>();
  private List<Join   >  joins       = new ArrayList<Join>(  );

  private List<WhereClause> whereClauses    = new ArrayList<WhereClause >();
  private List<String>      groupByColumns  = new ArrayList<String      >();
  private List<Sort>        sortColumns     = new ArrayList<Sort        >();

  private Integer limit;
  private Integer offset;

  // Constructor
  public SqlQueryObj( String sql )
    throws JSQLParserException
  {

    CCJSqlParserManager parserRealSql = new CCJSqlParserManager();

    Statement stmt = parserRealSql.parse(new StringReader(sql)); // create a jSqlParser Statement from the sql

    if (stmt instanceof Select) { // only parse select sql
      Select selectStatement = (Select) stmt; //convert to Select Statement
      PlainSelect ps = (PlainSelect)selectStatement.getSelectBody();

      List<SelectItem> selectItems = ps.getSelectItems();
      selectItems.stream().forEach(selectItem -> {
        String itemStr = selectItem.toString();
        columns.add( itemStr );
      } ); // add the selected items to result

      FromItem fromItem = ps.getFromItem();
      String fromItemStr = fromItem.toString();
      fromSources.add( new Source( fromItemStr ) );

      TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
      List<String> tableNames = tablesNamesFinder.getTableList(stmt);
      System.out.println( tableNames );
      tableNames.stream().forEach(tableName -> {
        // System.out.println( fromItemStr + " " + tableName );
        if( ! fromItemStr.equals( tableName ) ){
          fromSources.add( new Source( tableName ) );
        }
      } ); // add the table names to result

      joins = ps.getJoins();
      whereClauses.add( new WhereClause( ps ) );

      // The 'group by' clause
      GroupByElement groupByElement = ps.getGroupBy();
      if( ! Objects.isNull( groupByElement) ){
        List<Expression> groupByExpressions = groupByElement.getGroupByExpressions();
          groupByExpressions.stream().forEach( groupByExpression -> {
          groupByColumns.add( groupByExpression.toString() );
        } );
      }

      // The 'order by' clause
      List<OrderByElement> orderByElements = ps.getOrderByElements();
      if( ! Objects.isNull( orderByElements) ){
          orderByElements.stream().forEach( orderByElement -> {
          sortColumns.add( new Sort( orderByElement ) );
        } );
      }

      // The 'limit n,m' clause
      Limit limitObj = ps.getLimit();
      if( ! Objects.isNull( limitObj) ){
        Expression limitExpr  = limitObj.getRowCount();
        Expression offsetExpr = limitObj.getOffset();

        if( null != limitExpr ){
          limit = Integer.parseInt( limitExpr.toString() );
        }
        if( null != offsetExpr ){
          offset = Integer.parseInt( offsetExpr.toString() );
        }


      }

    }
    //  return list;

  }

  // Getters
  public List<String> getColumns(){     return columns;     }
  public List<Source> getFromSources(){ return fromSources; }
  public List<Join> getJoins(){         return joins; }

  public List<WhereClause>  getWhereClauses(  ){  return whereClauses;    }
  public List<String>       getGroupByColumns(){  return groupByColumns;  }
  public List<Sort>         getSortColumns(   ){  return sortColumns;     }

  public Integer getLimit(  ){ return limit;  }
  public Integer getOffset( ){ return offset; }
}

/**
 * Variable type to keep a single value out of FROM sql clause
 */
class Source {
  private String fromSource;
  public String getFromSource(){ return fromSource; }
  public Source( String fromSourceStr ){ fromSource = fromSourceStr; }
}

/**
 * Variable type to keep a single value out of WHERE sql clause
 */
class WhereClause {
  private Expression whereExpr;
  public Expression getWhereExpr(){ return whereExpr; }
  public WhereClause( PlainSelect ps){ whereExpr = ps.getWhere(); }
}

/**
 * Variable type to keep a single value out of single ORDER BY sql clause
 */
class Sort {
  private OrderByElement orderByElementProp;
  public OrderByElement getOrderByElementProp(){ return orderByElementProp; }

  public Sort( OrderByElement orderByElement ){
    orderByElementProp = orderByElement;
  }
}
