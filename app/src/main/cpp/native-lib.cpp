#include <jni.h>
#include <string>
#include "muParser/muParser.h"
#include <stdlib.h>
#include <chrono>

using namespace std;
using namespace mu;

void GetJStringContent(JNIEnv *AEnv, jstring AStr, string &ARes) {
    if (!AStr) {
        ARes.clear();
        return;
    }
    const char *s = AEnv->GetStringUTFChars(AStr,NULL);
    ARes=s;
    AEnv->ReleaseStringUTFChars(AStr,s);
}

/*
z^2-y^3+x
y^2-z-2*x
x^4-y*z-1
*/

double numerical_differentiation(double x, Parser &p, double &v)
{
    double const delta = 1E-12;
    v = x + delta;
    double x1 = p.Eval();
    v = x - delta;
    double x2 = p.Eval();
    return (x1 - x2) / (2.0 * delta);
}

vector<double> deltaX(vector<vector<double> > m, vector<double> ft)
{
    vector<double> d(ft.size());
    double di;
    for(int i = 0; i < m.size(); i++)
    {
        di = 0;
        for(int j = 0; j < ft.size(); j++)
        {
            di += m[i][j]*ft[j];
        }
        d[i] = di;
    }
    return d;
}

double determinant(vector<vector<double> > m)
{
    if (m.size() == 0 || m.size() != m[0].size()) return 0;
    if (m.size() == 1) return m[0][0];
    if (m.size() == 2) return m[0][0]*m[1][1] - m[0][1]*m[1][0];

    double d = 0;
    vector<vector<double> > m1(m.size()-1, vector<double>(m.size()-1));
    int mj, mk;
    for (int i = 0; i < m.size(); i++)
    {
        mj = 0;
        for (int j = 1; j < m.size(); j++)
        {
            mk = 0;
            for (int k = 0; k < m.size(); k++)
            {
                if (i == k) continue;
                m1[mj][mk] = m[j][k];
                mk++;
            }
            mj++;
        }
        d += determinant(m1)*pow(-1,i+2)*m[0][i];
    }
    return d;
}

vector<vector<double> > invertedMatrix(vector<vector<double> > m)
{
    vector<vector<double> > m1(m.size(), vector<double>(m.size()));
    if (m.size() == 1)
    {
        m1[0][0] = 1 / m[0][0];
        return m1;
    }
    vector<vector<double> > m2(m.size() - 1, vector<double>(m.size() - 1));
    int mk, ml;
    double det  = determinant(m);
    if (det == 0) return m;
    for (int i = 0; i < m.size(); i++)
    {
        for (int j = 0; j < m.size(); j++)
        {
            mk = 0;
            for (int k = 0; k < m.size(); k++)
            {
                ml = 0;
                if (i == k) continue;
                for (int l = 0; l < m.size(); l++)
                {
                    if (j == l) continue;
                    m2[mk][ml] = m[k][l];
                    ml++;
                }
                mk++;
            }
            m1[j][i] = determinant(m2)*pow(-1,i+2)*pow(-1,j+2)/det;
        }
    }
    return m1;
}

bool isInteger(char c)
{
    return (c >= '0') && (c <= '9');
}

bool isLetter(char c)
{
    return (c >= 'a') && (c <= 'z');
}

double CAToDouble(string s, int si) //CA - Char Array
{
    double d = 0;
    int ci = 0;
    string str = "";
    while (s[si+ci]=='.'|| isInteger(s[si+ci]))
    {
        str += s[si+ci];
        ci++;
        if(s.length() - 1 < ci + si) break;
    }
    if (str.length() != 0) return atof(str.c_str());
    return d;
}

vector<vector<double> > calcJMatrix(vector<Parser> p, vector<double> &x)
{
    vector<vector<double> > d(p.size(), vector<double>(p.size()));
    int c = 0;
    for(int i = 0; i < p.size(); i++)
    {
        for(int j = 0; j < p.size(); j++)
        {
            d[i][j] = numerical_differentiation(x[j], p[i], x[j]);
            c++;
        }
    }
    return d;
}

vector<double> evalSystem(vector<Parser> p, bool negative)
{
    vector<double> d(p.size());
    for(int i = 0; i < p.size(); i++)
    {
        d[i] = p[i].Eval();
    }
    if (negative) for(int i = 0; i < d.size(); i++) d[i] *= -1;
    return d;
}

void roundArray(vector<vector<double> > &v){
    for(int i = 0; i < v.size(); i++){
        for(int j = 0; j < v[i].size(); j++){
            if(abs(round(v[i][j]) - v[i][j]) < 1E-9) v[i][j] = round(v[i][j]);
            if(v[i][j] == -0) v[i][j] = 0;
        }
    }
}

bool isDoubleArraysEqual(vector<double> arr1, vector<double> arr2){
    if((arr1.size() == 0) || (arr2.size() == 0)) return false;
    for(int i = 0; i < arr1.size(); i++) if(abs(arr1[i]-arr2[i]) > 1E-8)return false;
    return true;
}

extern "C" jstring Java_com_orventro_equationsystem_MainActivity_solveEquationSystem
        (JNIEnv *env, jobject , jobjectArray stringArray, jfloat acc, jboolean add_info){
    int sysSize = env->GetArrayLength(stringArray);
    vector<string> expr(sysSize);
    vector<Parser> p(sysSize);
    vector<double> x(sysSize, 1);
    vector<char> xChar(sysSize, 0);
    ostringstream output;
    int accuracy = (int) pow(10, (float) acc);
    double MDeg = 1, MNum = 0;
    jstring jstr;
    auto start = chrono::high_resolution_clock::now();
    int wrong_res_num = 0;
    int repeated_res_num = 0;

    for(int i = 0; i < sysSize; i++)
    {
        jstr = (jstring) (env->GetObjectArrayElement(stringArray, i));
        GetJStringContent(env, jstr, expr[i]);
        expr[i] = " " + expr[i] + " ";
        p[i].SetExpr(expr[i]);
        for(int j = 0; j < expr[i].size(); j++)
        {
            if (j == 0 || (!isInteger(expr[i][j-1]) & expr[i][j-1] != '.')) MNum = max(MNum, abs(CAToDouble(expr[i], j)));

            if (!isLetter(expr[i][j]) && isLetter(expr[i][j + 1]) && !isLetter(expr[i][j + 2]))
            {
                for (int k = 0; k < sysSize; k++)
                {
                    if (xChar[k] == expr[i][j + 1]){
                        break;
                    }
                    if (xChar[k] == 0)
                    {
                        xChar[k] = expr[i][j + 1];
                        break;
                    }
                }
            }
        }
    }

    for(int i = 0; i < sysSize; i++)
    {
        for(int j = 0; j < sysSize; j++)
        {
            try {
                p[i].DefineVar(string(1, xChar[j]), &x[j]);
            } catch (Parser::exception_type &e){
                output << "E";
                return env->NewStringUTF(output.str().c_str());
            }
        }
        for (int j = 0; j < expr[i].length(); j++)
        {
            if(expr[i][j] == '^') MDeg = max(MDeg, CAToDouble(expr[i], j+1));
        }
    }

    for(int i = 0; i < sysSize; i++){
        try {
            p[i].Eval();
        } catch (Parser::exception_type &e){
            output << "E" << i+1;
            return env->NewStringUTF(output.str().c_str());
        }

    }

    int cn;
    double difr = 0;
    vector<double> dx;
    vector<vector<double> > roots((int)(accuracy * MDeg + 1), vector<double>(sysSize, 0));
    for(int i = 0; i < (int)(accuracy * MDeg + 1); i++)
    {
        for(int j = 0; j < sysSize; j++)
        {
            x[j] = -MNum * 2 + i * MNum * 4 / accuracy / MDeg + pow(-1,i+j);
        }
        cn = 0;
        while((( abs(difr) > 1E-12) | (cn < 2) ) & (cn < 100))
        {
            difr = 0;
            dx = deltaX(invertedMatrix(calcJMatrix(p, x)), evalSystem(p, true));
            for(int j = 0; j < sysSize; j++)
            {
                difr += dx[j];
                x[j] += dx[j];
            }
            cn++;
        }
        for(int j = 0; j < sysSize; j++) {
            roots[i][j] = x[j];
        }
    }
    int rs = 0;
    roundArray(roots);
    vector<double> zeroArray(sysSize, 0);
    vector<double> calcResult(sysSize);
    for(int i = 0; i < roots.size(); i++){
        for(int j = 0; j < sysSize; j++){
            if(roots[i][j] != roots[i][j]) { // == NAN
                wrong_res_num++;
                roots[i].clear();
                break;
            }
            x[j] = roots[i][j];
        }
        if(roots[i].size() == 0) continue;
        calcResult = evalSystem(p, false);
        for(int j = 0; j < sysSize; j++){
            if(calcResult[j] != calcResult[j]) { // == NAN
                wrong_res_num++;
                roots[i].clear();
                break;
            }
        }
        if(!isDoubleArraysEqual(calcResult, zeroArray)){
            wrong_res_num++;
            roots[i].clear();
        } else {
            for (int j = 0; j < i; j++) {
                if (isDoubleArraysEqual(roots[i], roots[j])) {
                    repeated_res_num++;
                    roots[i].clear();
                    break;
                }
            }
        }
        if(roots[i].size() != 0)rs++;
    }
    int ri = 0;
    vector<vector<double> > roots1(rs, vector<double>(sysSize));
    for (int i = 0; i < roots.size(); i++) {
        if (roots[i].size() != 0) {
            roots1[ri] = roots[i];
            ri++;
        }
    }
    roots = roots1;
    output.precision(15);
    if(roots.size() == 0) output << "N";
    for(int i = 0; i < roots.size(); i++){
        for(int j = 0; j < roots[i].size(); j++){
            output << xChar[j] << i + 1 << "\u202f=\u202f" << roots[i][j];
            if(j < roots[i].size()-1) output << ",   ";
        }
        if(roots.size() - i > 1) output << ";\n------------------------------\n";
    }
    auto finish = chrono::high_resolution_clock::now();

    if(add_info) {
        output
        << "\n\n calculation time : "
        << chrono::duration_cast<std::chrono::nanoseconds>(finish-start).count() << " ns , \n"
        << "\n tryings to find root : " << (int)(accuracy * MDeg + 1) << '\n'
        << "\n wrong results (not showed): " << wrong_res_num << " , "
        << wrong_res_num / (accuracy * MDeg + 1) * 100 << "%\n"
        << "\n repeated results (not showed):" << repeated_res_num << " , "
        << repeated_res_num / (accuracy * MDeg + 1) * 100 << "%\n"
        << "\n showed results :" << roots.size() << " , "
        << roots.size() / (accuracy * MDeg + 1) * 100 << "%\n";
    }

    return env->NewStringUTF(output.str().c_str());
}
